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
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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
        IGNORE_LOGGING("ignore.logging"),
        ENABLE_EAGER_DATASTORE_STATS("enable.eager.datastore.stats", new RegexpValue("(sync|async)"));

        private String key;
        private Value value;

        private Feature(String key) {
            this(key, Boolean.TRUE.toString());
        }

        private Feature(String key, String value) {
            this(key, new DefaultValue(value));
        }

        private Feature(String key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    private static Map<ClassLoader, Compatibility> instances = new WeakHashMap<ClassLoader, Compatibility>();
    private static ThreadLocal<Set<Feature>> temps = new ThreadLocal<Set<Feature>>();

    private final Properties properties;
    private final Map<Feature, Boolean> values = new ConcurrentHashMap<Feature, Boolean>();

    private Compatibility(Properties properties) {
        this.properties = properties;
    }

    public static Compatibility getInstance() {
        final ClassLoader cl = Application.getAppClassloader();
        return getInstance(cl);
    }

    public static synchronized Compatibility getInstance(final ClassLoader cl) {
        try {
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
        return isTempEnabled(feature) || isEnabledInternal(Feature.ENABLE_ALL) || isEnabledInternal(feature);
    }

    public String getValue(Feature feature) {
        return properties.getProperty(feature.key);
    }

    protected boolean isEnabledInternal(Feature feature) {
        Boolean result = values.get(feature);
        if (result == null) {
            final String value = properties.getProperty(feature.key);
            result = (value != null && feature.value.match(value));
            values.put(feature, result);
        }
        return result;
    }

    private static boolean isTempEnabled(Feature feature) {
        Set<Feature> features = temps.get();
        return features != null && features.contains(feature);
    }

    /**
     * Temp enable feature.
     *
     * @param feature the feature
     */
    public static void enable(Feature feature) {
        Set<Feature> features = temps.get();
        if (features == null) {
            features = new HashSet<Feature>();
            temps.set(features);
        }
        features.add(feature);
    }

    /**
     * Disable feature.
     *
     * @param feature the feature
     */
    public static void disable(Feature feature) {
        Set<Feature> features = temps.get();
        if (features != null) {
            features.remove(feature);
            if (features.isEmpty()) {
                temps.remove();
            }
        }
    }

    private static interface Value {
        boolean match(String value);
    }

    private static class DefaultValue implements Value {
        private String value;

        private DefaultValue(String value) {
            this.value = value;
        }

        public boolean match(String v) {
            return value.equalsIgnoreCase(v);
        }
    }

    private static class RegexpValue implements Value {
        private Pattern pattern;

        private RegexpValue(String value) {
            this.pattern = Pattern.compile(value);
        }

        public boolean match(String v) {
            return pattern.matcher(v).matches();
        }
    }
}
