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

package org.jboss.test.capedwarf.search;

import com.google.appengine.api.search.query.QueryTreeBuilder;
import com.google.appengine.api.search.query.QueryTreeVisitor;
import com.google.appengine.api.search.query.QueryTreeWalker;
import com.google.appengine.repackaged.org.antlr.runtime.RecognitionException;
import com.google.appengine.repackaged.org.antlr.runtime.tree.CommonTree;
import com.google.appengine.repackaged.org.antlr.runtime.tree.Tree;
import junit.framework.Assert;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.jboss.capedwarf.search.QueryConverter;
import org.junit.Test;


/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class QueryConverterTestCase {

    public static final Version LUCENE_VERSION = Version.LUCENE_35;

    @Test
    public void testConversion() throws Exception {
        assertQueryEquals("field:value", "field:value");
        assertQueryEquals("-field:value", "NOT field:value");
        assertQueryEquals("field:aaa AND field:bbb", "field:aaa field:bbb");
        assertQueryEquals("field:aaa AND field:bbb AND field:ccc", "field:aaa field:bbb field:ccc");
        assertQueryEquals("field:aaa OR field:bbb OR field:ccc", "field:aaa OR field:bbb OR field:ccc");
        assertQueryEquals("field:aaa AND field:bbb AND field:ccc", "field:aaa AND field:bbb AND field:ccc");
        assertQueryEquals("author:rose OR (NOT body:filigree)", "author:rose OR NOT body:filigree");
        assertQueryEquals("author:rose AND (NOT body:filigree)", "author:rose AND NOT body:filigree");

        assertQueryEquals("field:[value TO value]", "field=value");
        assertQueryEquals("all:rose", "rose");
        assertQueryEquals("bob:hope OR bob:dope", "bob:(hope OR dope)");
        assertQueryEquals("bob:hope AND bob:dope", "bob:(hope AND dope)");
        assertQueryEquals("bob:hope AND bob:dope", "bob:(hope dope)");
        assertQueryEquals("bob:[hope TO hope] OR bob:[dope TO dope]", "bob=(hope OR dope)");
//
        assertQueryEquals("field:[12 TO 12]", "field=12");
        assertQueryEquals("field:{12 TO *}", "field>12");
        assertQueryEquals("field:[12 TO *]", "field>=12");
        assertQueryEquals("field:{* TO 12}", "field<12");
        assertQueryEquals("field:[* TO 12]", "field<=12");

        assertQueryEquals("field:{aaa TO *}", "field>aaa");
        assertQueryEquals("field:[aaa TO *]", "field>=aaa");
        assertQueryEquals("field:[* TO ddd]", "field<=ddd");
        assertQueryEquals("field:{* TO ddd}", "field<ddd");

        assertQueryEquals("field:\"any other name\"", "field:\"any other name\"");
        assertQueryEquals("all:\"any other name\"", "\"any other name\"");
    }

    @Test
    public void testCaseIsHandledCorrectly() throws Exception {
        assertQueryEquals("author:\"Rose Bloom\"", "author:\"Rose Bloom\"");
        assertQueryEquals("author:Rose", "author:Rose");
    }

    @Test
    public void testExamplesFromGAEDocumentation() throws Exception {
        assertQueryEquals("all:rose", "rose");
        assertQueryEquals("all:\"any other name\"", "\"any other name\"");
        assertQueryEquals("field:value", "field:value");
        assertQueryEquals("field:\"value as a string\"", "field:\"value as a string\"");
        assertQueryEquals("author:rose", "author:rose");
        assertQueryEquals("body:\"any other name\"", "body:\"any other name\"");
        assertQueryEquals("author:\"Rose Jones\" AND body:rose", "author:\"Rose Jones\" body:rose");
        assertQueryEquals("price:{* TO 100}", "price<100");
        assertQueryEquals("sent:[2011-02-28 TO *]", "sent>=2011-02-28");
        assertQueryEquals("product_code:[xyz1000 TO xyz1000]", "product_code = xyz1000");

        assertQueryEquals("author:bob OR ((author:rose OR author:tom) AND author:jones)", "author:(bob OR ((rose OR tom) AND jones))");
        assertQueryEquals("author:rose AND (NOT body:filigree)", "author:rose NOT body:filigree");
        assertQueryEquals("(author:Thomas OR author:Jones) AND (NOT body:rose)", "(author:Thomas OR author:Jones) AND (NOT body:rose)");

    }

    private static void assertQueryEquals(String expectedLuceneQueryString, String gaeQueryString) throws ParseException {
        dumpTree(gaeQueryString);
        Query query = new QueryConverter(gaeQueryString).convert();
        Query expectedQuery = new QueryParser(LUCENE_VERSION, null, new StandardAnalyzer(LUCENE_VERSION)).parse(expectedLuceneQueryString);
        System.out.println("expectedQuery = " + expectedQuery + "    (" + expectedQuery.getClass() + ")");
        System.out.println("query = " + query + "    (" + query.getClass() + ")");
        Assert.assertEquals(expectedQuery, query);
    }


    private static void dumpTree(String query) {
        try {
            System.out.println("------------------------------------------------------------");
            System.out.println("query = " + query);
            CommonTree tree = new QueryTreeBuilder().parse(query);
            Tree simplifiedTree = QueryTreeWalker.simplify(tree);
            new QueryTreeWalker(new MyQueryTreeVisitor()).walk(simplifiedTree, 0);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MyQueryTreeVisitor implements QueryTreeVisitor<Integer> {
        public void visitSequence(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("sequence", walker, tree, level);
        }

        public void visitConjunction(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("conjunction", walker, tree, level);
        }

        public void visitDisjunction(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("disjunction", walker, tree, level);
        }

        public void visitNegation(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("negation", walker, tree, level);
        }

        public void visitRestriction(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("restriction", walker, tree, level);
        }

        public void visitFuzzy(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("fuzzy", walker, tree, level);
        }

        public void visitLiteral(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("literal", walker, tree, level);
        }

        public void visitLessThan(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("<", walker, tree, level);
        }

        public void visitLessOrEqual(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("<=", walker, tree, level);
        }

        public void visitGreaterThan(QueryTreeWalker walker, Tree tree, Integer level) {
            visit(">", walker, tree, level);
        }

        public void visitGreaterOrEqual(QueryTreeWalker walker, Tree tree, Integer level) {
            visit(">=", walker, tree, level);
        }

        public void visitEqual(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("=", walker, tree, level);
        }

        public void visitContains(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("contains", walker, tree, level);
        }

        public void visitValue(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("value", walker, tree, level);
        }

        public void visitOther(QueryTreeWalker walker, Tree tree, Integer level) {
            visit("other", walker, tree, level);
        }

        private void visit(String type, QueryTreeWalker walker, Tree tree, Integer level) {
            System.out.println(indent(level) + type + " " + tree.getText() + " (" + tree + ")");
            for (int i=0; i<tree.getChildCount(); i++) {
                Tree child = tree.getChild(i);
                walker.walk(child, level+1);
            }
        }

        private String indent(Integer level) {
            StringBuilder sbuf = new StringBuilder();
            for (int i=0; i<level; i++) {
                sbuf.append("  ");
            }
            return sbuf.toString();
        }
    }

}
