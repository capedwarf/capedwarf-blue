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

package org.jboss.capedwarf.common.reflection;

import java.lang.reflect.Method;

/**
 * Cache target invocation.
 *
 * @param <T> exact return type
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TargetInvocation<T> {
    private final Method method;
    private Object[] args;

    TargetInvocation(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public TargetInvocation resetArgs(Object[] args) {
        this.args = args;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T invoke(final Object target) throws Exception {
        final Class<?> clazz = method.getDeclaringClass();
        if (clazz.isInstance(target) == false)
            throw new IllegalArgumentException("Target " + target + " is not assignable to " + clazz);

        return (T)method.invoke(target, args);
    }
}
