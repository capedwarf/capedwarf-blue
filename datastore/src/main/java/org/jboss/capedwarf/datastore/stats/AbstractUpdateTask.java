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

package org.jboss.capedwarf.datastore.stats;

import java.util.Collections;
import java.util.Map;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.infinispan.BaseTxTask;

/**
 * Abstract update task.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractUpdateTask<V> extends BaseTxTask<String, V, Entity> {
    private final CapedwarfEnvironment env;
    private final Update update;

    public AbstractUpdateTask(Update update) {
        this.env = CapedwarfEnvironment.getThreadLocalInstance();
        this.update = update;
    }

    protected Entity callInTx() throws Exception {
        CapedwarfEnvironment previous = CapedwarfEnvironment.setThreadLocalInstance(env);
        try {
            return callInTxInternal();
        } finally {
            if (previous != null) {
                CapedwarfEnvironment.setThreadLocalInstance(previous);
            } else {
                CapedwarfEnvironment.clearThreadLocalInstance();
            }
        }
    }

    private Entity callInTxInternal() throws Exception {
        final DatastoreService service = DatastoreServiceFactory.getDatastoreService();

        final String cacheKey = update.statsKind() + update.statsNamespace();

        lock(cacheKey);

        V value = getCache().get(cacheKey);
        Key key = provideKey(value);
        Entity entity;

        String oldNamespace = NamespaceManager.get();
        NamespaceManager.set(update.statsNamespace());
        try {
            entity = getOrCreateEntity(service, key);
            entity = update.update(entity);
            key = service.put(entity);
        } finally {
            NamespaceManager.set(oldNamespace);
        }

        getCache().put(cacheKey, updateValue(value, key));
        return entity;
    }

    private void lock(String cacheKey) {
        AdvancedCache<String, V> ac = getCache().getAdvancedCache();
        if (ac.lock(cacheKey) == false)
            throw new IllegalArgumentException("Cannot get a lock on key for " + cacheKey);
    }

    private Entity getOrCreateEntity(DatastoreService service, Key key) {
        if (key == null) {
            return createNewEntity();
        } else {
            Map<Key, Entity> map = service.get(null, Collections.singleton(key));
            if (map.isEmpty()) {
                return createNewEntity();
            } else {
                return map.values().iterator().next();
            }
        }
    }

    private Entity createNewEntity() {
        Entity entity = new Entity(update.statsKind());
        update.initialize(entity);
        return entity;
    }

    protected abstract Key provideKey(V value);

    protected abstract V updateValue(V value, Key key);
}