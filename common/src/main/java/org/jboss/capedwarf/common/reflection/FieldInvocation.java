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

package org.jboss.capedwarf.common.reflection;

import java.lang.reflect.Field;

/**
 * Cache field invocation.
 *
 * @param <T> exact return type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FieldInvocation<T> {
    private Object target;
    private final Field field;

    FieldInvocation(Field field) {
        this.field = field;
    }

    FieldInvocation(Object target, Field field) {
        this(field);
        this.target = target;
    }

    public T invoke() {
        return invoke(target);
    }

    @SuppressWarnings("unchecked")
    public T invoke(final Object object) {
        try {
            return (T) field.get(object);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw RuntimeException.class.cast(e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
