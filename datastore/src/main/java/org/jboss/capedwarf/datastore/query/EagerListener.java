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

import java.util.Map;
import java.util.WeakHashMap;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.infinispan.AdvancedCache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.config.JBossEnvironment;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.datastore.QueryTypeFactories;

/**
 * Eager Listener.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Listener
public class EagerListener {
    private static final Map<ClassLoader, Boolean> MARKER = new WeakHashMap<ClassLoader, Boolean>();
    private final JBossEnvironment env = JBossEnvironment.getThreadLocalInstance();

    static synchronized void registerListener(AdvancedCache<Key, Entity> cache) {
        ClassLoader cl = Application.getAppClassloader();
        if (MARKER.containsKey(cl) == false) {
            MARKER.put(cl, true);
            cache.addListener(new EagerListener());
        }
    }

    // can be invoked async, from jgroups
    protected void executeUpdate(Update update) {
        JBossEnvironment previous = JBossEnvironment.setThreadLocalInstance(env);
        try {
            InfinispanUtils.submit(Application.getAppId(), CacheName.DIST, update.toCallable(), update.statsKind());
        } finally {
            if (previous != null) {
                JBossEnvironment.setThreadLocalInstance(previous);
            } else {
                JBossEnvironment.clearThreadLocalInstance();
            }
        }
    }

    @CacheEntryModified
    public void onPut(CacheEntryModifiedEvent<Key, Entity> event) {
        if (event.isOriginLocal() == false)
            return;

        final Key key = event.getKey();
        if (QueryTypeFactories.isSpecialKind(key.getKind()))
            return;

        Entity trigger = event.getValue();
        if (event.isPre() == false) {
            executeUpdate(new TotalStatsPutUpdate(trigger));
            executeUpdate(new KindStatsPutUpdate(trigger));
        } else if (trigger != null) {
            // was existing entity modified
            executeUpdate(new TotalStatsRemoveUpdate(trigger));
            executeUpdate(new KindStatsRemoveUpdate(trigger));
        }
    }

    @CacheEntryRemoved
    public void onRemove(CacheEntryRemovedEvent<Key, Entity> event) {
        if (event.isPre() == false || event.isOriginLocal() == false)
            return;

        Key key = event.getKey();
        if (QueryTypeFactories.isSpecialKind(key.getKind()) == false) {
            Entity trigger = event.getValue();
            executeUpdate(new TotalStatsRemoveUpdate(trigger));
            executeUpdate(new KindStatsRemoveUpdate(trigger));
        }
    }
}
