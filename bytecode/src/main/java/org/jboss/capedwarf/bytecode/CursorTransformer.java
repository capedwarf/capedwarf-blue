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
import org.jboss.capedwarf.datastore.query.LazySize;

/**
 * Hack Cursor class.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CursorTransformer extends RewriteTransformer {
    protected void transformInternal(CtClass clazz) throws Exception {
        final ClassPool pool = clazz.getClassPool();
        CtClass intClass = pool.get(int.class.getName());

        // add index field, ctor, getter

        CtField indexField = CtField.make("private java.util.concurrent.atomic.AtomicInteger index;", clazz);
        clazz.addField(indexField);

        CtField sizeField = CtField.make("private org.jboss.capedwarf.datastore.query.LazySize size;", clazz);
        clazz.addField(sizeField);

        CtConstructor ctor = new CtConstructor(new CtClass[]{pool.get(AtomicInteger.class.getName()), pool.get(LazySize.class.getName())}, clazz);
        ctor.setBody("{this.index = $1; this.size = $2;}");
        clazz.addConstructor(ctor);

        CtMethod getIndex = new CtMethod(intClass, "getIndex", new CtClass[]{}, clazz);
        getIndex.setModifiers(Modifier.PUBLIC);
        getIndex.setBody("{return index.get();}");
        clazz.addMethod(getIndex);

        CtMethod getSize = new CtMethod(intClass, "getSize", new CtClass[]{}, clazz);
        getSize.setModifiers(Modifier.PRIVATE);
        getSize.setBody("{return size.getSize();}");
        clazz.addMethod(getSize);

        // override other methods

        CtConstructor cloneCtor = clazz.getDeclaredConstructor(new CtClass[]{clazz});
        cloneCtor.setBody("this($1.index, $1.size);");

        CtMethod writeObject = clazz.getDeclaredMethod("writeObject", new CtClass[]{pool.get(ObjectOutputStream.class.getName())});
        writeObject.setBody("{$1.writeInt(getIndex()); $1.writeInt(getSize());}");

        CtMethod readObject = clazz.getDeclaredMethod("readObject", new CtClass[]{pool.get(ObjectInputStream.class.getName())});
        readObject.setBody("{index = new java.util.concurrent.atomic.AtomicInteger($1.readInt()); size = new org.jboss.capedwarf.datastore.query.DirectLazySize($1.readInt());}");

        CtMethod advance = clazz.getDeclaredMethod("advance", new CtClass[]{intClass, pool.get(PreparedQuery.class.getName())});
        advance.setBody("{index.addAndGet($1); return this;}");

        CtMethod reverse = clazz.getDeclaredMethod("reverse");
        reverse.setBody("return new com.google.appengine.api.datastore.Cursor(new java.util.concurrent.atomic.AtomicInteger(getSize() - getIndex()), size);");

        CtMethod toWebSafeString = clazz.getDeclaredMethod("toWebSafeString");
        toWebSafeString.setBody("return index + \",\" + getSize();");

        CtMethod fromWebSafeString = clazz.getDeclaredMethod("fromWebSafeString", new CtClass[]{pool.get(String.class.getName())});
        fromWebSafeString.setBody(
                "{" +
                "   String[] split = ($1 != null && $1.length() > 0) ? $1.split(\",\") : new String[0];" +
                "   int i = (split.length > 0) ? Integer.parseInt(split[0]) : 0;" +
                "   int s = (split.length > 0) ? Integer.parseInt(split[1]) : 0;" +
                "   return new com.google.appengine.api.datastore.Cursor(new java.util.concurrent.atomic.AtomicInteger(i), new org.jboss.capedwarf.datastore.query.DirectLazySize(s));" +
                "}"
        );

        CtMethod toByteString = clazz.getDeclaredMethod("toByteString", new CtClass[]{});
        toByteString.setBody("return com.google.appengine.repackaged.com.google.protobuf.ByteString.copyFrom(toWebSafeString().getBytes());");

        CtMethod equals = clazz.getDeclaredMethod("equals", new CtClass[]{pool.get(Object.class.getName())});
        equals.setBody("return ($1 instanceof com.google.appengine.api.datastore.Cursor) && ((com.google.appengine.api.datastore.Cursor) $1).getIndex() == getIndex();");

        CtMethod hashCode = clazz.getDeclaredMethod("hashCode");
        hashCode.setBody("return getIndex();");

        CtMethod toString = clazz.getDeclaredMethod("toString");
        toString.setBody("return \"Cursor:\" + index + \",\" + getSize();");
    }

    protected boolean doCheck(CtClass clazz) throws NotFoundException {
        return clazz.getDeclaredField("index") != null;
    }
}
