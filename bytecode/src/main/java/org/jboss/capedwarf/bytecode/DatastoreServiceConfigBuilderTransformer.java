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

import javassist.CtClass;
import javassist.CtMethod;

/**
 * A bytecode hack + reflection hack.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DatastoreServiceConfigBuilderTransformer extends JavassistTransformer {
    protected void transform(CtClass clazz) throws Exception {
        CtMethod method = clazz.getDeclaredMethod("withDefaults");
        method.setBody("{" +
                "java.lang.Object callbacks = null;" +
                "ClassLoader cl = Thread.currentThread().getContextClassLoader();" +
                "java.io.InputStream is = cl.getResourceAsStream(\"/META-INF/datastorecallbacks.xml\");" +
                "if (is != null) {" +
                "   callbacks = new com.google.appengine.api.datastore.DatastoreCallbacksImpl(is, false);" +
                "}" +
                "try {" +
                "   Class clazz = com.google.appengine.api.datastore.DatastoreService.class.getClassLoader().loadClass(\"com.google.appengine.api.datastore.DatastoreServiceConfig\");" +
                "   if (callbacks != null) {" +
                "       java.lang.reflect.Constructor ctor = clazz.getDeclaredConstructor(new Class[]{com.google.appengine.api.datastore.DatastoreCallbacks.class});" +
                "       ctor.setAccessible(true);" +
                "       return ((com.google.appengine.api.datastore.DatastoreServiceConfig)ctor.newInstance(new Object[]{callbacks}));" +
                "   } else {" +
                "       java.lang.reflect.Constructor ctor;" +
                "       try {" +
                "           ctor = clazz.getDeclaredConstructor(new Class[]{com.google.appengine.api.datastore.DatastoreCallbacks.class});" +
                "           ctor.setAccessible(true);" +
                "           return ((com.google.appengine.api.datastore.DatastoreServiceConfig)ctor.newInstance(new Object[]{callbacks}));" +
                "       } catch (Exception ignored) {" +
                "           ctor = clazz.getDeclaredConstructor(new Class[0]);" +
                "           ctor.setAccessible(true);" +
                "           return ((com.google.appengine.api.datastore.DatastoreServiceConfig)ctor.newInstance(new Object[0]));" +
                "       }" +
                "   }" +
                "} catch (Exception e) {" +
                "   throw new RuntimeException(e);" +
                "}" +
                "}");
    }
}
