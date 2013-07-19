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

package org.jboss.capedwarf.bytecode;

import java.sql.Connection;
import java.util.Properties;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DriverTransformer extends JavassistTransformer {
    protected void transform(CtClass clazz) throws Exception {
        ClassPool pool = clazz.getClassPool();

        CtClass[] params = {pool.get(String.class.getName()), pool.get(Properties.class.getName())};

        CtMethod method = clazz.getDeclaredMethod("connect", params);
        clazz.removeMethod(method);

        // change the return type to plain Connection
        CtMethod newMethod = new CtMethod(pool.get(Connection.class.getName()), "connect", params, clazz);
        newMethod.setBody("{return org.jboss.capedwarf.sql.SqlUtils.connect($1, $2);}");
        clazz.addMethod(newMethod);
    }
}
