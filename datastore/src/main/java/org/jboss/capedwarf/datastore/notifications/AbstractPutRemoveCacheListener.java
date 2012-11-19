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

package org.jboss.capedwarf.datastore.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.jboss.capedwarf.datastore.QueryTypeFactories;

/**
 * Abstract put/remove cache listener
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractPutRemoveCacheListener extends AbstractCacheListener {

    @CacheEntryModified
    public void onPut(CacheEntryModifiedEvent<Key, Entity> event) {
        if (isIgnoreEvent(event))
            return;

        Entity trigger = event.getValue();
        if (event.isPre() == false) {
            onPostPut(trigger);
        } else if (trigger != null) {
            // was existing entity modified
            onPrePut(trigger);
        }
    }

    @CacheEntryRemoved
    public void onRemove(CacheEntryRemovedEvent<Key, Entity> event) {
        if (isIgnoreEvent(event))
            return;

        if (event.isPre()) {
            Entity trigger = event.getValue();
            onPreRemove(trigger);
        } else {
            onPostRemove(event.getKey());
        }
    }

    /**
     * Do we ignore event.
     *
     * @param event the event
     * @return true if we ignore event, false otherwise
     */
    protected boolean isIgnoreEvent(CacheEntryEvent<Key, Entity> event) {
        return event.isOriginLocal() == false || isIgnoreEntry(event.getKey());
    }

    /**
     * Do we ignore entry.
     *
     * @param key the key
     * @return true if we ignore entry, false otherwise
     */
    protected boolean isIgnoreEntry(Key key) {
        return QueryTypeFactories.isSpecialKind(key.getKind());
    }

    /**
     * Pre put / modification, trigger is the old value.
     *
     * @param trigger the trigger entity
     */
    protected abstract void onPrePut(Entity trigger);

    /**
     * Post put / modification, trigger is the new value.
     *
     * @param trigger the trigger entity
     */
    protected abstract void onPostPut(Entity trigger);

    /**
     * Pre remove, trigger is the old value.
     *
     * @param trigger the trigger entity
     */
    protected abstract void onPreRemove(Entity trigger);

    /**
     * Post remove.
     *
     * @param key the trigger key
     */
    @SuppressWarnings("UnusedParameters")
    protected void onPostRemove(Key key) {
        // do nothing by default
    }

}
