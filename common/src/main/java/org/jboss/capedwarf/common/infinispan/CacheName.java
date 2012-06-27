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

package org.jboss.capedwarf.common.infinispan;

import com.google.appengine.api.datastore.Entity;

/**
 * Available caches in CapeDwarf.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public enum CacheName {
    DEFAULT("default", true, new CacheIndexing(1).setOffset(-1).addClass(Entity.class)),
    DATA("data", false),
    METADATA("metadata", false),
    MEMCACHE("memcache", false),
    DIST("dist", false),
    TASKS("tasks", true, new CacheIndexing(1).addClass("org.jboss.capedwarf.tasks.TaskOptionsEntity")),
    SEARCH("search", true, new CacheIndexing(-1).setOffset(1).addClass("org.jboss.capedwarf.search.CacheValue")),
    PROSPECTIVE_SEARCH("prospective_search", true, new CacheIndexing(-1).addClass("org.jboss.capedwarf.prospectivesearch.SubscriptionHolder"));

    private String name;
    private boolean config;
    private CacheIndexing indexing;

    private CacheName(String name, boolean config) {
        this.name = name;
        this.config = config;
    }

    private CacheName(String name, boolean config, CacheIndexing indexing) {
        this(name, config);
        this.indexing = indexing;
    }

    public String getName() {
        return name;
    }

    public boolean hasConfig() {
        return config;
    }

    public CacheIndexing getIndexing() {
        return indexing;
    }
}
