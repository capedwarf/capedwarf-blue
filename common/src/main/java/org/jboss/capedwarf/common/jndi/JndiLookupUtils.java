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

package org.jboss.capedwarf.common.jndi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javassist.util.proxy.MethodHandler;
import org.jboss.capedwarf.shared.bytecode.BytecodeUtils;
import org.jboss.capedwarf.shared.util.Utils;

/**
 * JNDI lookup should be limited.
 * Preferred way of lookup is via ComponentRegistry.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JndiLookupUtils {
    private static final Logger log = Logger.getLogger(JndiLookupUtils.class.getName());

    protected static Properties findProperties(String propertiesName) throws IOException {
        Properties jndiProperties = new Properties();
        ClassLoader cl = Utils.getAppClassLoader();
        URL jndiPropertiesURL = cl.getResource(propertiesName);
        if (jndiPropertiesURL != null) {
            InputStream is = jndiPropertiesURL.openStream();
            try {
                jndiProperties.load(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
        return jndiProperties;
    }

    protected static Object checkNames(Context ctx, String... names) {
        for (String jndiName : names) {
            try {
                return ctx.lookup(jndiName);
            } catch (NamingException ne) {
                String msg = "Unable to retrieve object from JNDI [" + jndiName + "]";
                log.fine(msg + ": " + ne);
            }
        }
        throw new IllegalStateException("Cannot find JNDI object: " + Arrays.toString(names));
    }

    public static <T> T lookup(String propertyKey, Class<T> expected, String... names) {
        if (propertyKey == null)
            throw new IllegalArgumentException("Null property key.");
        if (expected == null)
            throw new IllegalArgumentException("Null expected class");

        Context ctx = null;
        try {
            Properties properties = findProperties("jndi.properties");
            ctx = new InitialContext(properties);

            Object result;
            String jndiNamespace = properties.getProperty(propertyKey);
            if (jndiNamespace != null)
                result = checkNames(ctx, jndiNamespace);
            else
                result = checkNames(ctx, names);

            log.fine("Using JNDI found " + expected.getName() + ": " + result);
            return expected.cast(result);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Unable to retrieve " + expected.getName() + " from JNDI [" + propertyKey + "]";
            log.info(msg + ": " + e);
            throw new IllegalStateException(msg);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ne) {
                    log.warning("Unable to release initial context: " + ne);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T lazyLookup(final String propertyKey, final Class<T> expected, final String... names) {
        if (propertyKey == null)
            throw new IllegalArgumentException("Null property key.");
        if (expected == null)
            throw new IllegalArgumentException("Null expected class");

        if (expected.isInterface()) {
            return expected.cast(Proxy.newProxyInstance(expected.getClassLoader(), new Class[]{expected}, new InvocationHandler() {
                private volatile Object delegate;

                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (delegate == null) {
                        synchronized (this) {
                            if (delegate == null)
                                delegate = lookup(propertyKey, expected, names);
                        }
                    }

                    return method.invoke(delegate, args);
                }
            }));
        } else {
            final MethodHandler handler = new MethodHandler() {
                private volatile Object delegate;

                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    if (delegate == null) {
                        synchronized (this) {
                            if (delegate == null)
                                delegate = lookup(propertyKey, expected, names);
                        }
                    }

                    return thisMethod.invoke(delegate, args);
                }
            };
            return BytecodeUtils.proxy(expected, handler);
        }
    }
}
