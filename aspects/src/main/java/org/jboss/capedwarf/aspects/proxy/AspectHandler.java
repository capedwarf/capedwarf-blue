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

package org.jboss.capedwarf.aspects.proxy;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class AspectHandler implements MethodHandler {
    private Class<?> apiInterface;
    private Object apiImpl;

    AspectHandler(Class<?> apiInterface, Object apiImpl) {
        this.apiInterface = apiInterface;
        this.apiImpl = apiImpl;
    }

    public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
        // hash code
        if ((args == null || args.length == 0) && "hashCode".equals(method.getName())) {
            return apiImpl.hashCode();
        }

        // equals
        if ((args != null && args.length == 1) && "equals".equals(method.getName())) {
            Object other = args[0];
            return other != null && apiImpl.equals(other);
        }

        // toString
        if ((args == null || args.length == 0) && "toString".equals(method.getName())) {
            return apiImpl.toString();
        }

        AspectInfo info = new AspectInfo(apiInterface, apiImpl, method, args);
        AspectContext context = new AspectContext(info);
        return context.proceed();
    }
}
