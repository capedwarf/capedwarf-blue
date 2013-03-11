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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;

/**
 * Reflection access.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ReflectionLineRewriter extends ClassLineRewriter {
    private static final SetMultimap<String, String> interceptedMethods;

    static {
        List<String> fieldMethods = Arrays.asList(
                "get",
                "getBoolean",
                "getByte",
                "getChar",
                "getDouble",
                "getFloat",
                "getInt",
                "getLong",
                "getShort",
                "set",
                "setBoolean",
                "setByte",
                "setChar",
                "setDouble",
                "setFloat",
                "setInt",
                "setLong",
                "setShort"
        );
        interceptedMethods = HashMultimap.create();
        interceptedMethods.put("java.lang.reflect.Method", "invoke");
        interceptedMethods.putAll("java.lang.reflect.Field", fieldMethods);
        interceptedMethods.put("java.lang.reflect.Constructor", "newInstance");
        interceptedMethods.put("java.lang.Class", "newInstance");
    }

    protected void doVisit(LineContext context) throws Exception {
        String className = context.getClassName();
        Set<String> methods = interceptedMethods.get(className);
        if (methods != null && methods.isEmpty() == false) {
            String name = getName(context.getConstPool(), context.getVal());
            if (name != null && methods.contains(name)) {
                Bytecode bytecode = new Bytecode(context.getConstPool());
                String desc = getDesc(context.getConstPool(), context.getVal());
                if (context.getOp() == CodeIterator.INVOKEVIRTUAL) {
                    desc = Descriptor.insertParameter(className, desc);
                }
                bytecode.addInvokestatic(Restrictions.class.getName(), name, desc);
                context.write(bytecode);
            }
        }
    }
}
