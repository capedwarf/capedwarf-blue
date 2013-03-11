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

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;

/**
 * Rewrite lines context.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LineContext {
    private ConstPool pool;
    private CodeIterator cit;
    private int index;
    private String className;
    private String name;

    LineContext(ConstPool pool, CodeIterator cit) {
        this.pool = pool;
        this.cit = cit;
    }

    public void insertAtIndex(byte[] bytes) throws Exception {
        cit.insertAt(index, bytes);
    }

    public ConstPool getPool() {
        return pool;
    }

    public CodeIterator getCit() {
        return cit;
    }

    public boolean hasNext() {
        return cit.hasNext();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getOp() {
        return cit.byteAt(index);
    }

    public int getVal() {
        return cit.s16bitAt(index + 1);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
