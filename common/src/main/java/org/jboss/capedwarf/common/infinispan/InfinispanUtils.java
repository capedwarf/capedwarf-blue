/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.common.util.Util;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InfinispanUtils {
    private static volatile int cacheManagerUsers;
    private static EmbeddedCacheManager cacheManager;
    private static final Map<String, GridFilesystem> gridFilesystems = new HashMap<String, GridFilesystem>();

    protected static EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }

    protected static <K, V> Cache<K, V> getCache(CacheName template, String appId) {
        final String cacheName = toCacheName(template, appId);
        final Cache<K, V> cache = cacheManager.getCache(cacheName, false);
        if (cache == null)
            throw new IllegalArgumentException("No such cache?! - " + cacheName);
        return cache;
    }

    protected static String toCacheName(CacheName template, String appId) {
        return template.getName() + "_" + appId;
    }

    public static <K, V> Cache<K, V> getCache(String appId, CacheName template) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        if (template == null)
            throw new IllegalArgumentException("Null template!");

        return getCache(template, appId);
    }

    /**
     * Submit the task to distributed execution env, expecting result at the end.
     */
    public static <R> R submit(final String appId, final CacheName template, final Callable<R> task, Object... keys) {
        final Future<R> result = distribute(appId, template, task, true, keys);
        return Util.quietGet(result);
    }

    /**
     * Submit the task to distributed execution env, it could be a fire-n-forget way.
     */
    public static <R> Future<R> fire(final String appId, final CacheName template, final Callable<R> task, Object... keys) {
        return distribute(appId, template, task, false, keys);
    }

    private static <R> Future<R> distribute(final String appId, final CacheName template, final Callable<R> task, final boolean direct, final Object... keys) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");

        final Cache cache = getCache(appId, template);
        final ExecutorService executor = (direct ? ExecutorFactory.getDirectExecutor() : ExecutorFactory.getInstance());
        final DistributedExecutorService des = new DefaultExecutorService(cache, executor);
        return des.submit(task, keys);
    }

    public static GridFilesystem getGridFilesystem(String appId) {
        GridFilesystem gfs = gridFilesystems.get(appId);
        if (gfs == null) {
            synchronized (gridFilesystems) {
                gfs = gridFilesystems.get(appId);
                if (gfs == null) {
                    final Cache<String, byte[]> data = getCache(appId, CacheName.DATA);
                    final Cache<String, GridFile.Metadata> metadata = getCache(appId, CacheName.METADATA);
                    gfs = new GridFilesystem(data, metadata);
                    gridFilesystems.put(appId, gfs);
                }
            }
        }
        return gfs;
    }

    @SuppressWarnings("UnusedParameters")
    public static synchronized void initApplicationData(String appId) {
        if (cacheManager == null) {
            cacheManager = ComponentRegistry.getInstance().getComponent(Keys.CACHE_MANAGER);
        }
        cacheManagerUsers++;
    }

    public static synchronized void clearApplicationData(String appId) {
        synchronized (gridFilesystems) {
            gridFilesystems.remove(appId);
        }
        cacheManagerUsers--;
        if (cacheManagerUsers == 0) {
            cacheManager = null;
        }
    }

    public static Address getLocalNode(String appId) {
        return getCache(appId, CacheName.DEFAULT).getAdvancedCache().getRpcManager().getAddress();
    }
}
