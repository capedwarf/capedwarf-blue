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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.capedwarf.common.app.Application;

/**
 * Reflection hacks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class ReflectionUtils {

    private static final Class[] EMPTY_CLASSES = new Class[0];
    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Load class.
     *
     * @param className the classname
     * @return loaded class or rutime exception
     */
    public static Class<?> loadClass(String className) {
        ClassLoader appCL = Application.getAppClassloader();
        Class<?> clazz = loadClass(appCL, className);
        if (clazz != null)
            return clazz;

        clazz = loadClass(ReflectionUtils.class.getClassLoader(), className);
        if (clazz != null)
            return clazz;

        throw new RuntimeException(new ClassNotFoundException(className));
    }

    /**
     * Create new instance.
     *
     * @param clazz the class
     * @return new instance
     */
    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, EMPTY_CLASSES, EMPTY_ARGS);
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

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Class[] types, Object[] args) {
        try {
            Class<?> clazz = Class.forName(className);
            return (T) newInstance(clazz, types, args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke no-param method.
     *
     * @param target     the object on which to invoke method
     * @param methodName the name of the method
     * @return value returned by invoked method
     */
    public static Object invokeInstanceMethod(Object target, String methodName) {
        return invokeInstanceMethod(target, methodName, EMPTY_CLASSES, EMPTY_ARGS);
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
     * Cache invocation.
     *
     * @param clazz      the class
     * @param methodName the method name
     * @return cached target invocation
     */
    public static <T> TargetInvocation<T> cacheInvocation(Class<?> clazz, String methodName) {
        return cacheInvocation(clazz, methodName, EMPTY_CLASSES, EMPTY_ARGS);
    }

    /**
     * Cache invocation.
     *
     * @param clazz      the class
     * @param methodName the method name
     * @param types      the types
     * @param args       the args
     * @return cached target invocation
     */
    public static <T> TargetInvocation<T> cacheInvocation(Class<?> clazz, String methodName, Class[] types, Object[] args) {
        final Method m = findMethod(clazz, methodName, types);
        return new TargetInvocation<T>(m, args);
    }

    /**
     * Get field value.
     *
     * @param target the target
     * @param fieldName the field name
     * @return field's value
     */
    public static Object getFieldValue(Object target, String fieldName) {
        if (target == null)
            throw new IllegalArgumentException("Null target");

        final Field field = findField(target.getClass(), fieldName);
        try {
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get static field value.
     *
     * @param clazz the class
     * @param fieldName the field name
     * @return field's value
     */
    public static Object getFieldValue(Class<?> clazz, String fieldName) {
        final Field field = findField(clazz, fieldName);
        try {
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set field value.
     * 
     * @param target the target
     * @param fieldName the field name
     * @param value the value
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null)
            throw new IllegalArgumentException("Null target");

        final Field field = findField(target.getClass(), fieldName);
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set static field value.
     * 
     * @param clazz the class
     * @param fieldName the field name
     * @param value the value
     */
    public static void setFieldValue(Class<?> clazz, String fieldName, Object value) {
        final Field field = findField(clazz, fieldName);
        try {
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Try loading the class.
     *
     * @param cl the classloader
     * @param className the classname
     * @return loaded class or null
     */
    private static Class<?> loadClass(ClassLoader cl, String className) {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
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
        final Method m = findMethod(clazz, methodName, types);
        try {
            return m.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find method.
     *
     * @param clazz      the class
     * @param methodName the method name
     * @param types      the types
     * @return method's return value
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class[] types) {
        if (clazz == null)
            throw new IllegalArgumentException("Null class");
        if (methodName == null)
            throw new IllegalArgumentException("Null method name");

        try {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    final Method m = current.getDeclaredMethod(methodName, types);
                    m.setAccessible(true);
                    return m;
                } catch (NoSuchMethodException ignored) {
                }
                current = current.getSuperclass();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        throw new IllegalStateException("Couldn't find method: " + clazz.getName() + " / " + methodName);
    }

    /**
     * Find field.
     *
     * @param clazz the class
     * @param fieldName the field name
     * @return field instance
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null)
            throw new IllegalArgumentException("Null class");
        if (fieldName == null)
            throw new IllegalArgumentException("Null field name");

        try {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    final Field f = current.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {
                }
                current = current.getSuperclass();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        throw new IllegalStateException("Couldn't find field: " + clazz.getName() + " / " + fieldName);
    }
}
