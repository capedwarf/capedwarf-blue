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

import java.util.Set;

import com.google.common.collect.Sets;
import javassist.CtClass;
import javassist.bytecode.Descriptor;

/**
 * Fix classloader parent.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ClassLoaderLineRewriter extends ClassLineRewriter {
    private static final String URL_CLASSLOADER_CLASS = "java.net.URLClassLoader";
    private static final String SECURE_CLASSLOADER_CLASS = "java.security.SecureClassLoader";
    private static final String CLASSLOADER_CLASS = "java.lang.ClassLoader";

    private static final String CONSTRUCTOR_METHOD = "<init>";
    private static final String NEW_INSTANCE_METHOD = "newInstance";

    private Set<String> CLASSLOADER_TYPES = Sets.newHashSet(
            URL_CLASSLOADER_CLASS,
            SECURE_CLASSLOADER_CLASS,
            CLASSLOADER_CLASS
    );

    protected boolean doVisit(LineContext context) throws Exception {
        String className = context.getClassName();
        String name = getName(context.getConstPool(), context.getVal());

        InitType type = initsNewClassLoader(className, name);
        if (type == InitType.None) {
            return false;
        }

        String desc = getDesc(context.getConstPool(), context.getVal());

        CtClass[] parameterTypes = Descriptor.getParameterTypes(desc, context.getClassPool());

        // TODO

        return true;
    }

    private InitType initsNewClassLoader(String className, String name) {
        if (CLASSLOADER_TYPES.contains(className) && name.equals(CONSTRUCTOR_METHOD)) {
            return InitType.Init;
        }

        if (className.equals(URL_CLASSLOADER_CLASS) && name.equals(NEW_INSTANCE_METHOD)) {
            return InitType.NewInstance;
        }

        return InitType.None;
    }

    private enum InitType {
        None,
        Init,
        NewInstance,
    }
}
