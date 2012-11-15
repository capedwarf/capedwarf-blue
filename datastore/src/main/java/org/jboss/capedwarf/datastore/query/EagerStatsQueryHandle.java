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

package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

/**
 * Eager query handle.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class EagerStatsQueryHandle extends AbstractQueryHandle {
    private static final EagerListener LISTENER = new EagerListener();

    EagerStatsQueryHandle(QueryHandleService service) {
        super(service);
    }

    void initialize(QueryHandleService service) {
        Cache cache = service.getCache();
        AdvancedCache ac = cache.getAdvancedCache();
        if (ac.getListeners().contains(LISTENER) == false) {
            ac.addListener(LISTENER);
        }
    }

    public PreparedQuery createQuery(Transaction tx, Query query) {
        return service.createQuery(tx, query); // just run the query
    }

    @Listener
    public static class EagerListener {
        @CacheEntryCreated
        public void onPut(CacheEntryCreatedEvent<Key, Entity> event) {

        }

        @CacheEntryRemoved
        public void onRemove(CacheEntryRemovedEvent<Key, Entity> event) {
        }
    }
}