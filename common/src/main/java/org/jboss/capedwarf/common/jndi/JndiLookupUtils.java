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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JndiLookupUtils {
    private static final Logger log = Logger.getLogger(JndiLookupUtils.class.getName());

    protected static Properties findProperties(String propertiesName) throws IOException {
        Properties jndiProperties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
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

    public static <T> T lookup(String propertyKey, Class<T> expected, String... names) throws IOException {
        if (propertyKey == null)
            throw new IllegalArgumentException("Null property key.");
        if (expected == null)
            throw new IllegalArgumentException("Null expected class");

        Properties properties = findProperties("jndi.properties");
        String jndiNamespace = properties.getProperty(propertyKey);
        Context ctx = null;
        try {
            ctx = new InitialContext(properties);

            Object result;
            if (jndiNamespace != null)
                result = checkNames(ctx, jndiNamespace);
            else
                result = checkNames(ctx, names);

            log.info("Using JNDI found " + expected.getName() + ": " + result);
            return expected.cast(result);
        } catch (Exception ne) {
            String msg = "Unable to retrieve " + expected.getName() + " from JNDI [" + jndiNamespace + "]";
            log.info(msg + ": " + ne);
            throw new IOException(msg);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ne) {
                    log.info("Unable to release initial context: " + ne);
                }
            }
        }
    }

    protected static Object checkNames(Context ctx, String... names) throws IOException {
        for (String jndiName : names) {
            try {
                return ctx.lookup(jndiName);
            } catch (NamingException ne) {
                String msg = "Unable to retrieve object from JNDI [" + jndiName + "]";
                log.fine(msg + ": " + ne);
            }
        }
        throw new IOException("Cannot find JNDI object: " + Arrays.toString(names));
    }

}
