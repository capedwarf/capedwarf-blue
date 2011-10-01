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

package org.jboss.capedwarf.images;

import java.lang.reflect.Field;

/**
 * Wraps any object and exposes its private fields through getValueOf() method.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ReflectionWrapper<WRAPPED> {
    protected WRAPPED wrapped;

    public ReflectionWrapper(WRAPPED wrapped) {
        this.wrapped = wrapped;
    }

    @SuppressWarnings({"unchecked"})
    protected <V> V getFieldValue(String fieldName) {
        return (V) getFieldValue(getAccessibleField(fieldName));
    }

    @SuppressWarnings("unchecked")
    protected <V> V getFieldValue(Field field) {
        try {
            return (V) field.get(wrapped);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Field getAccessibleField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Class " + clazz + " does not seem to contain field " + fieldName, e);
        }
    }

    protected Field getAccessibleField(String fieldName) {
        return getAccessibleField(wrapped.getClass(), fieldName);
    }
}
