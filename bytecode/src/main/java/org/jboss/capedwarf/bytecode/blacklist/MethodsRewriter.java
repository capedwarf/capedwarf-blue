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

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * Rewrite methods.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class MethodsRewriter implements Rewriter {
    private List<MethodRewriter> rewriters = new ArrayList<MethodRewriter>();

    MethodsRewriter() {
        rewriters.add(new CodeLineRewriter());
    }

    @SuppressWarnings("unchecked")
    public void visit(ClassFile file) throws Exception {
        List<MethodInfo> methods = file.getMethods();
        for (MethodInfo mi : methods) {
            try {
                // ignore abstract methods
                if (mi.getCodeAttribute() == null) {
                    continue;
                }

                boolean modified = false;
                for (MethodRewriter mr : rewriters) {
                    modified |= mr.visit(mi);
                }

                if (modified) {
                    mi.getCodeAttribute().computeMaxStack();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot rewrite method: " + mi, e);
            }
        }
    }
}
