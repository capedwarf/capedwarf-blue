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

package org.jboss.capedwarf.common.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Reflection hacks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class ReflectionUtils {

    /**
     * Create new instance.
     *
     * @param clazz the class
     * @return new instance
     */
    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, new Class[]{}, new Object[]{});
    }

    /**
     * Create new instance.
     *
     * @param clazz the class
     * @param types the ctor types
     * @param args  the ctor args
     * @return new instance
     */
    public static <T> T newInstance(Class<T> clazz, Class[] types, Object[] args) {
        if (clazz == null)
            throw new IllegalArgumentException("Null class");

        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Invoke method.
     *
     * @param target     the target
     * @param methodName the method name
     * @param type       the type
     * @param arg        the arg
     * @return method's return value
     */
    public static Object invokeInstanceMethod(Object target, String methodName, Class<?> type, Object arg) {
        return invokeInstanceMethod(target, methodName, new Class[]{type}, new Object[]{arg});
    }

    /**
     * Invoke method.
     *
     * @param target     the target
     * @param methodName the method name
     * @param types      the types
     * @param args       the args
     * @return method's return value
     */
    public static Object invokeInstanceMethod(Object target, String methodName, Class[] types, Object[] args) {
        if (target == null)
            throw new IllegalArgumentException("Null target");

        return invokeMethod(target, target.getClass(), methodName, types, args);
    }

    /**
     * Invoke static method.
     *
     * @param clazz      the class
     * @param methodName the method name
     * @param type       the type
     * @param arg        the arg
     * @return method's return value
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?> type, Object arg) {
        return invokeStaticMethod(clazz, methodName, new Class[]{type}, new Object[]{arg});
    }

    /**
     * Invoke static method.
     *
     * @param clazz      the class
     * @param methodName the method name
     * @param types      the types
     * @param args       the args
     * @return method's return value
     */
    @SuppressWarnings({"NullableProblems"})
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class[] types, Object[] args) {
        return invokeMethod(null, clazz, methodName, types, args);
    }

    /**
     * Invoke method.
     *
     * @param target     the target
     * @param clazz      the class
     * @param methodName the method name
     * @param types      the types
     * @param args       the args
     * @return method's return value
     */
    private static Object invokeMethod(Object target, Class<?> clazz, String methodName, Class[] types, Object[] args) {
        if (clazz == null)
            throw new IllegalArgumentException("Null class");
        if (methodName == null)
            throw new IllegalArgumentException("Null method name");

        try {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    Method m = clazz.getDeclaredMethod(methodName, types);
                    m.setAccessible(true);
                    return m.invoke(target, args);
                } catch (Exception ignored) {
                }
                current = current.getSuperclass();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        throw new IllegalStateException("Couldn't invoke method: " + clazz.getName() + " / " + methodName);
    }
}
