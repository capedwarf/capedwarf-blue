/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.search;

import com.google.appengine.api.search.query.QueryTreeBuilder;
import com.google.appengine.api.search.query.QueryTreeWalker;
import com.google.appengine.repackaged.org.antlr.runtime.RecognitionException;
import com.google.appengine.repackaged.org.antlr.runtime.tree.CommonTree;
import com.google.appengine.repackaged.org.antlr.runtime.tree.Tree;
import org.apache.lucene.search.Query;

import java.util.logging.Logger;

/**
 * Converts GAE Search query string into Lucene query
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class QueryConverter {

    private final Logger log = Logger.getLogger(getClass().getName());

    private final String allFieldName;

    public QueryConverter(String allFieldName) {
        this.allFieldName = allFieldName;
    }

    public Query convert(String queryString) {
//        log.info("Converting GAE query to Lucene query: " + queryString);
        try {
            CommonTree tree = new QueryTreeBuilder().parse(queryString);
            Tree simplifiedTree = QueryTreeWalker.simplify(tree);

//            dumpTreeToLog(simplifiedTree);

            final Query queryHolder[] = new Query[1];
            Context context = new Context() {
                @Override
                public void addSubQuery(Query query) {
                    queryHolder[0] = query;
                }
            };
            context.setFieldName(allFieldName);
            new QueryTreeWalker<Context>(new GAEQueryTreeVisitor()).walk(simplifiedTree, context);
            return queryHolder[0];
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpTreeToLog(Tree tree) {
        PrintingQueryTreeVisitor visitor = new PrintingQueryTreeVisitor() {
            @Override
            protected void print(String text) {
                log.info(text);
            }
        };

        new QueryTreeWalker<Integer>(visitor).walk(tree, 0);
    }
}
