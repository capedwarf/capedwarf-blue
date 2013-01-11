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

package org.jboss.capedwarf.common.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class Backends implements Serializable, Iterable<Backends.Backend> {
    private static final long serialVersionUID = 1L;

    private Map<String, Backend> backends = new LinkedHashMap<String, Backend>();

    public Backend getBackend(String name) {
        return backends.get(name);
    }

    public Iterator<Backend> iterator() {
        return backends.values().iterator();
    }

    protected void addBackend(Backend backend) {
        backends.put(backend.getName(), backend);
    }

    public static enum Class {
        B1, B2, B4, B8
    }

    public static enum Options {
        DYNAMIC, FAIL_FAST, PUBLIC
    }

    public static class Backend implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Class clazz = Class.B2;
        private int instances = 1;
        private int maxConcurrentRequests;
        private Set<Options> options = Collections.emptySet();

        private volatile Pattern pattern;

        private Pattern getPattern() {
            if (pattern == null) {
                pattern = Pattern.compile(name + "|[0-9]+\\." + name);
            }
            return pattern;
        }

        public boolean matches(String backend) {
            return getPattern().matcher(backend).matches();
        }

        public String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }

        public Class getClazz() {
            return clazz;
        }

        protected void setClazz(Class clazz) {
            this.clazz = clazz;
        }

        public int getInstances() {
            return instances;
        }

        protected void setInstances(int instances) {
            this.instances = instances;
        }

        public int getMaxConcurrentRequests() {
            return maxConcurrentRequests;
        }

        protected void setMaxConcurrentRequests(int maxConcurrentRequests) {
            this.maxConcurrentRequests = maxConcurrentRequests;
        }

        public Set<Options> getOptions() {
            return options;
        }

        protected void setOptions(Set<Options> options) {
            this.options = options;
        }
    }
}
