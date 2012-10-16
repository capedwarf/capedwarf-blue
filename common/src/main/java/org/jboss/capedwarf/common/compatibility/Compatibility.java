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

package org.jboss.capedwarf.common.compatibility;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.jboss.capedwarf.common.app.Application;

/**
 * Allow for custom extensions to GAE API, impl, behavior, etc.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class Compatibility {
    public static enum Feature {
        ENABLE_ALL("enable.all"),
        DISABLE_ENTITY_GROUPS("disable.entity.groups"),
        IGNORE_TO_LONG_CONVERSION("ignore.long.conversion"),
        IGNORE_LOGGING("ignore.logging");

        private String key;
        private String value;
        private Boolean enabled;

        private Feature(String key) {
            this(key, Boolean.TRUE.toString());
        }

        private Feature(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private static Map<ClassLoader, Compatibility> instances = new WeakHashMap<ClassLoader, Compatibility>();

    private final Properties properties;

    private Compatibility(Properties properties) {
        this.properties = properties;
    }

    public static synchronized Compatibility getInstance() {
        try {
            final ClassLoader cl = Application.getAppClassloader();
            Compatibility compatibility = instances.get(cl);
            if (compatibility == null) {
                final Properties properties = new Properties();
                properties.putAll(System.getProperties());
                final InputStream is = cl.getResourceAsStream("capedwarf-compatibility.properties");
                if (is != null) {
                    try {
                        properties.load(is);
                    } finally {
                        is.close();
                    }
                }
                compatibility = new Compatibility(properties);
                instances.put(cl, compatibility);
            }
            return compatibility;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isEnabled(Feature feature) {
        return isEnabledInternal(Feature.ENABLE_ALL) || isEnabledInternal(feature);
    }

    protected boolean isEnabledInternal(Feature feature) {
        synchronized (feature) {
            if (feature.enabled == null) {
                final String value = properties.getProperty(feature.key);
                feature.enabled = (value != null && value.equalsIgnoreCase(feature.value));
            }
        }
        return feature.enabled;
    }
}
