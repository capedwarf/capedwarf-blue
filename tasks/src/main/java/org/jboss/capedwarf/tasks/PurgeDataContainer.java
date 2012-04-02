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

package org.jboss.capedwarf.tasks;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import com.google.appengine.api.taskqueue.Queue;
import org.infinispan.container.DefaultDataContainer;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionThreadPolicy;

/**
 * Custom purge.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class PurgeDataContainer extends DefaultDataContainer {
    private static final Field entries;
    
    static {
        try {
            entries = DefaultDataContainer.class.getDeclaredField("entries");
            entries.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException(t);           
        }
    }

    private Map map;
    private Queue queue;

    PurgeDataContainer(int concurrencyLevel, int maxEntries, EvictionStrategy strategy, EvictionThreadPolicy policy, Queue queue) {
        super(concurrencyLevel, maxEntries, strategy, policy);
        this.map = map();
        this.queue = queue;
    }

    private Map map() {
        try {
            return (Map) entries.get(this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public void purgeExpired() {
        long currentTimeMillis = System.currentTimeMillis();
        for (Iterator<InternalCacheEntry> purgeCandidates = map.values().iterator(); purgeCandidates.hasNext();) {
            InternalCacheEntry e = purgeCandidates.next();
            if (e.isExpired(currentTimeMillis)) {
                purgeCandidates.remove();
                Object value = e.getValue();
                if (value instanceof TaskLeaseEntity) {
                    TaskLeaseEntity tle = (TaskLeaseEntity) value;
                    queue.add(tle.getOptions());
                }
            }
        }
    }
}
