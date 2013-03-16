/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
import java.util.Set;
import java.util.TreeSet;

import javassist.bytecode.CodeIterator;

/**
 * Invocation and class check.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class InvokeRewriter extends AbstractLineRewriter {
    private static final Set<Integer> OPS;

    static {
        OPS = new TreeSet<Integer>();
        // invocations
        OPS.add(CodeIterator.INVOKEINTERFACE);
        OPS.add(CodeIterator.INVOKESPECIAL);
        OPS.add(CodeIterator.INVOKESTATIC);
        OPS.add(CodeIterator.INVOKEVIRTUAL);
        // fields
        OPS.add(CodeIterator.NEW);
    }

    private List<LineRewriter> rewriters = new ArrayList<LineRewriter>();

    InvokeRewriter() {
        rewriters.add(new ObjectAccessLineRewriter());
        rewriters.add(new ReflectionLineRewriter());
    }

    public int visit(LineContext context) throws Exception {
        int modified = 0;
        int op = context.getOp();
        if (isRef(op) && context.hasNext()) {
            int val = context.getVal();
            String className = getClassName(context.getConstPool(), val);
            if (className != null) {
                context.setClassName(className);
                for (LineRewriter rewriter : rewriters) {
                    modified += rewriter.visit(context);
                }
                return modified;
            }
        }
        return modified;
    }

    protected boolean isRef(int op) {
        return OPS.contains(op);
    }
}
