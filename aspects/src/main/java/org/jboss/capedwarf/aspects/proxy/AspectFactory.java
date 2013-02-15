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
import java.security.AccessController;
import java.security.PrivilegedAction;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.jboss.capedwarf.common.util.Util;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class AspectFactory {
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> apiInterface, T apiImpl) {
        try {
            final ProxyFactory factory = new ProxyFactory();
            factory.setFilter(FINALIZE_FILTER);
            factory.setInterfaces(new Class[]{apiInterface});
            factory.setSuperclass(apiImpl.getClass()); // expose impl
            // ProxyFactory already caches classes
            Class<?> proxyClass = getProxyClass(factory);
            ProxyObject proxyObject = (ProxyObject) proxyClass.newInstance();
            MethodHandler handler = new AspectHandler(apiInterface, apiImpl);
            proxyObject.setHandler(handler);
            return (T) proxyObject;
        } catch (Throwable t) {
            throw Util.toRuntimeException(t);
        }
    }

    protected static Class<?> getProxyClass(ProxyFactory factory) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null)
            return factory.createClass();
        else
            return AccessController.doPrivileged(new ClassCreator(factory));
    }

    /**
     * Privileged class creator.
     */
    protected static class ClassCreator implements PrivilegedAction<Class<?>> {
        private ProxyFactory factory;

        public ClassCreator(ProxyFactory factory) {
            this.factory = factory;
        }

        public Class<?> run() {
            return factory.createClass();
        }
    }

    private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
        public boolean isHandled(Method m) {
            // skip finalize methods
            return !("finalize".equals(m.getName()) && m.getParameterTypes().length == 0);
        }
    };
}
