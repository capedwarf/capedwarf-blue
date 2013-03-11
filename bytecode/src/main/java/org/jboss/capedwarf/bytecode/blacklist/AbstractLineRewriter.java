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

import javassist.bytecode.ConstPool;

/**
 * Abstract line rewriter.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
abstract class AbstractLineRewriter implements LineRewriter {
    protected String getClassName(ConstPool pool, int i) {
        int tag = pool.getTag(i);
        if (tag == ConstPool.CONST_Methodref) {
            return pool.getMethodrefClassName(i);
        } else if (tag == ConstPool.CONST_InterfaceMethodref) {
            return pool.getInterfaceMethodrefClassName(i);
        } else if (tag == ConstPool.CONST_Fieldref) {
            return pool.getFieldrefClassName(i);
        } else {
            return null; // cannot read class
        }
    }

    protected String getName(ConstPool pool, int i) {
        int tag = pool.getTag(i);
        if (tag == ConstPool.CONST_Methodref) {
            return pool.getMethodrefName(i);
        } else if (tag == ConstPool.CONST_InterfaceMethodref) {
            return pool.getInterfaceMethodrefName(i);
        } else if (tag == ConstPool.CONST_Fieldref) {
            return pool.getFieldrefName(i);
        } else {
            return null; // cannot read class
        }
    }
}
