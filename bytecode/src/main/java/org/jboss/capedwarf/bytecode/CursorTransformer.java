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

package org.jboss.capedwarf.bytecode;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.appengine.api.datastore.PreparedQuery;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Hack Cursor class.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CursorTransformer extends JavassistTransformer {
    protected void transform(CtClass clazz) throws Exception {
        // allow for multiple bytecode runs
        if (isAlreadyModified(clazz))
            return;

        final ClassPool pool = clazz.getClassPool();
        CtClass intClass = pool.get(int.class.getName());

        // add index field, ctor, getter

        CtField indexField = CtField.make("private java.util.concurrent.atomic.AtomicInteger index;", clazz);
        clazz.addField(indexField);

        CtConstructor ctor = new CtConstructor(new CtClass[]{pool.get(AtomicInteger.class.getName())}, clazz);
        ctor.setBody("{this.index = $1;}");
        clazz.addConstructor(ctor);

        CtMethod getIndex = new CtMethod(intClass, "getIndex", new CtClass[]{}, clazz);
        getIndex.setModifiers(Modifier.PUBLIC);
        getIndex.setBody("{return index.get();}");
        clazz.addMethod(getIndex);

        // override other methods

        CtConstructor cloneCtor = clazz.getDeclaredConstructor(new CtClass[]{clazz});
        cloneCtor.setBody("this($1.index);");

        CtMethod writeObject = clazz.getDeclaredMethod("writeObject", new CtClass[]{pool.get(ObjectOutputStream.class.getName())});
        writeObject.setBody("$1.writeInt(getIndex());");

        CtMethod readObject = clazz.getDeclaredMethod("readObject", new CtClass[]{pool.get(ObjectInputStream.class.getName())});
        readObject.setBody("index = new java.util.concurrent.atomic.AtomicInteger($1.readInt());   ");

        CtMethod advance = clazz.getDeclaredMethod("advance", new CtClass[]{intClass, pool.get(PreparedQuery.class.getName())});
        advance.setBody("index.addAndGet($1);");

        CtMethod reverse = clazz.getDeclaredMethod("reverse");
        reverse.setBody("return new com.google.appengine.api.datastore.Cursor(new java.util.concurrent.atomic.AtomicInteger((-1) * getIndex()));");

        CtMethod toWebSafeString = clazz.getDeclaredMethod("toWebSafeString");
        toWebSafeString.setBody("return index.toString();");

        CtMethod fromWebSafeString = clazz.getDeclaredMethod("fromWebSafeString", new CtClass[]{pool.get(String.class.getName())});
        fromWebSafeString.setBody("return new com.google.appengine.api.datastore.Cursor(new java.util.concurrent.atomic.AtomicInteger(Integer.parseInt($1)));");

        CtMethod fromByteArray = clazz.getDeclaredMethod("fromByteArray", new CtClass[]{pool.get(byte[].class.getName())});
        fromByteArray.setBody("return fromWebSafeString(new String($1));");

        CtMethod equals = clazz.getDeclaredMethod("equals", new CtClass[]{pool.get(Object.class.getName())});
        equals.setBody("return ($1 instanceof com.google.appengine.api.datastore.Cursor) && ((com.google.appengine.api.datastore.Cursor) $1).getIndex() == getIndex();");

        CtMethod hashCode = clazz.getDeclaredMethod("hashCode");
        hashCode.setBody("return getIndex();");

        CtMethod toString = clazz.getDeclaredMethod("toString");
        toString.setBody("return \"Cursor:\" + index;");
    }

    protected boolean isAlreadyModified(CtClass clazz) {
        try {
            return clazz.getDeclaredField("index") != null;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
