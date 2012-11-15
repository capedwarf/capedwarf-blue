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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.infinispan.BaseTxTask;

/**
 * Update last entity key.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UpdateKeyTask extends BaseTxTask<String, Key, Entity> {
    private Update update;

    public UpdateKeyTask(Update update) {
        this.update = update;
    }

    protected Entity callInTx() throws Exception {
        final AdvancedCache<String, Key> ac = getCache().getAdvancedCache();
        final String cacheKey = update.statsKind();

        if (ac.lock(cacheKey) == false)
            throw new IllegalArgumentException("Cannot get a lock on key for " + cacheKey);

        DatastoreService service = DatastoreServiceFactory.getDatastoreService();
        Entity entity;
        Key key = getCache().get(cacheKey);
        if (key == null) {
            entity = new Entity(update.statsKind());
            update.initialize(entity);
        } else {
            entity = service.get(key);
        }
        entity = update.update(entity);
        service.put(entity);
        getCache().put(cacheKey, entity.getKey());
        return entity;
    }
}