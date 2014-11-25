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

package org.jboss.capedwarf.admin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * @author Ales Justin
 */
class VelocityUtils {
    private static final Map<Key, String> MAP = new HashMap<Key, String>();

    static {
        MAP.put(new Key("org.", "apache.", "commons."), "org.jboss.capedwarf.apache.commons.");
        MAP.put(new Key("org.", "apache.", "velocity."), "org.jboss.capedwarf.apache.velocity.");
    }

    static String toString(Object value) {
        return (value == null) ? "" : value.toString();
    }

    static VelocityEngine create(ServletContext context) throws Exception {
        VelocityEngine engine = new VelocityEngine();

        engine.setApplicationAttribute(ServletContext.class.getName(), context);

        Properties props = defaultVelocityProperties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        props.put("tools.toolbox", "application");
        props.put("tools.application.esc", "org.apache.velocity.tools.generic.EscapeTool");
        engine.init(props);

        return engine;
    }

    private static Properties defaultVelocityProperties() throws IOException {
        Properties properties = new JarJarProperties();
        InputStream inputStream = RuntimeInstance.class.getResourceAsStream('/' + RuntimeConstants.DEFAULT_RUNTIME_PROPERTIES);
        try {
            properties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return properties;
    }

    private static String jarjar(String property) {
        for (Map.Entry<Key, String> e : MAP.entrySet()) {
            String key = e.getKey().toString();
            String value = e.getValue();
            property = property.replace(key, value);
        }
        return property;
    }

    private static class JarJarProperties extends Properties {
        public synchronized Object put(Object key, Object value) {
            return super.put(key, jarjar(value.toString()));
        }
    }

    // workaround jarjar
    private static class Key {
        private List<String> tokens = new ArrayList<String>();

        private Key(String... tokens) {
            this.tokens = Arrays.asList(tokens);
        }

        public String toString() {
            String s = "";
            for (String t : tokens) {
                s += t;
            }
            return s;
        }
    }
}
