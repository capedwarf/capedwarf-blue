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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Hack SystemProperty class.
 * Not used anymore, we hacked global system properties in CD_AS.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SystemPropertyTransformer extends RewriteTransformer {
    protected void transformInternal(final CtClass clazz) throws Exception {
        final CtField valueField = CtField.make("private java.lang.String value;", clazz);
        clazz.addField(valueField);

        final CtMethod get = clazz.getDeclaredMethod("get");
        get.setBody("{return value;}");

        final ClassPool pool = clazz.getClassPool();
        final CtMethod set = clazz.getDeclaredMethod("set", new CtClass[]{pool.get(String.class.getName())});
        set.setBody("{value = $1;}");
    }

    protected boolean doCheck(final CtClass clazz) throws NotFoundException {
        return clazz.getDeclaredField("value") != null;
    }
}
