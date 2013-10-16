/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.io.ByteArrayInputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class JavassistTransformer extends AbstractClassFileTransformer {
    public byte[] transform(final ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = new ClassPool() {
                @Override
                public ClassLoader getClassLoader() {
                    return loader;
                }
            };
            pool.appendClassPath(new LoaderClassPath(loader));
            CtClass clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            transform(clazz);
            return clazz.toBytecode();
        } catch (Exception e) {
            throw new IllegalClassFormatException(e.getMessage());
        }
    }

    protected abstract void transform(CtClass clazz) throws Exception;

    protected String toProxy(Class<?> apiInterface, String apiImp) {
        return toProxy(apiInterface.getName(), apiImp);
    }

    protected String toProxy(String apiInterface, String apiImp) {
        return "return org.jboss.capedwarf.aspects.proxy.AspectFactory.createProxy(" + apiInterface + ".class, " + apiImp + ");";
    }

    /**
     * Get nested class.
     *
     * @param outer outer class
     * @param name nested class name
     * @return nested class or exception if not found
     * @throws Exception for any error
     */
    protected static CtClass getNestedClass(CtClass outer, String name) throws Exception {
        // all nested
        CtClass[] allNested = outer.getNestedClasses();
        // get nested class
        CtClass nested = null;
        for (CtClass n : allNested) {
            String nestedName = n.getName();
            if (nestedName.endsWith("$" + name)) {
                nested = n;
                break;
            }
        }
        if (nested == null) {
            throw new IllegalArgumentException("No such nested class: " + name + " - " + Arrays.toString(allNested));
        }
        return nested;
    }
}
