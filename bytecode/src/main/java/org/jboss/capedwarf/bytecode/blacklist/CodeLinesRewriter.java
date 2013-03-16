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

package org.jboss.capedwarf.bytecode.blacklist;

import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

/**
 * Rewrite code lines.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class CodeLinesRewriter implements MethodRewriter {
    private List<LineRewriter> rewriters = new ArrayList<LineRewriter>();

    CodeLinesRewriter() {
        rewriters.add(new InvokeRewriter());
    }

    public int visit(MethodInfo mi) throws Exception {
        ConstPool pool = mi.getConstPool();
        CodeIterator cit = mi.getCodeAttribute().iterator();
        LineContext context = new LineContext(pool, cit);
        int modified = 0;
        while (cit.hasNext()) {
            // loop through the bytecode
            context.setIndex(cit.next());
            for (LineRewriter rewriter : rewriters) {
                modified += rewriter.visit(context);
            }
        }
        return modified;
    }
}
