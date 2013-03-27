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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Hack RawValue class.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RawValueTransformer extends RewriteTransformer {
    protected void transformInternal(CtClass clazz) throws Exception {
        final ClassPool pool = clazz.getClassPool();
        CtClass objectClass = pool.get(Object.class.getName());

        // add index field, ctor, getter

        CtField indexField = CtField.make("private org.jboss.capedwarf.datastore.RawValue delegate;", clazz);
        clazz.addField(indexField);

        CtConstructor ctor = new CtConstructor(new CtClass[]{objectClass}, clazz);
        ctor.setBody("{this.delegate = new org.jboss.capedwarf.datastore.RawValue($1);}");
        clazz.addConstructor(ctor);

        // override other methods
        CtMethod getValue = clazz.getDeclaredMethod("getValue", new CtClass[]{});
        getValue.setBody("return delegate.getValue();");

        CtMethod asType = clazz.getDeclaredMethod("asType", new CtClass[]{pool.get(Class.class.getName())});
        asType.setBody("return delegate.asType($1);");

        CtMethod asStrictType = clazz.getDeclaredMethod("asStrictType", new CtClass[]{pool.get(Class.class.getName())});
        asStrictType.setBody("return delegate.asStrictType($1);");

        CtMethod writeObject = clazz.getDeclaredMethod("writeObject", new CtClass[]{pool.get(ObjectOutputStream.class.getName())});
        writeObject.setBody("$1.writeObject(delegate);");

        CtMethod readObject = clazz.getDeclaredMethod("readObject", new CtClass[]{pool.get(ObjectInputStream.class.getName())});
        readObject.setBody("delegate = (org.jboss.capedwarf.datastore.RawValue)$1.readObject();");

        CtMethod equals = clazz.getDeclaredMethod("equals", new CtClass[]{pool.get(Object.class.getName())});
        equals.setBody("return ($1 instanceof com.google.appengine.api.datastore.RawValue) && ((com.google.appengine.api.datastore.RawValue) $1).delegate.equals(delegate);");

        CtMethod hashCode = clazz.getDeclaredMethod("hashCode");
        hashCode.setBody("return delegate.hashCode();");

        CtMethod toString = clazz.getDeclaredMethod("toString");
        toString.setBody("return \"RawValue:\" + getValue();");
    }

    protected boolean doCheck(CtClass clazz) throws NotFoundException {
        return clazz.getDeclaredField("delegate") != null;
    }
}
