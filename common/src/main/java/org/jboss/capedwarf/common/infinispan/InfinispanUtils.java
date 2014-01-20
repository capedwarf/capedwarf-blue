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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.distexec.DistributedTask;
import org.infinispan.distexec.DistributedTaskBuilder;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.common.async.Wrappers;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Key;
import org.jboss.capedwarf.shared.components.Keys;
import org.jboss.capedwarf.shared.components.SimpleKey;
import org.jboss.capedwarf.shared.util.Utils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InfinispanUtils {
    protected static EmbeddedCacheManager getCacheManager() {
        EmbeddedCacheManager manager = ComponentRegistry.getInstance().getComponent(Keys.CACHE_MANAGER);
        if (manager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        return manager;
    }

    protected static <K, V> Cache<K, V> getCache(CacheName template, String appId) {
        final String cacheName = toCacheName(template, appId);
        final Cache<K, V> cache = getCacheManager().getCache(cacheName, false);
        if (cache == null)
            throw new IllegalArgumentException("No such cache?! - " + cacheName);
        return cache;
    }

    protected static String toCacheName(CacheName template, String appId) {
        return template.getName() + "_" + appId;
    }

    private static <R> Future<R> distribute(final String appId, final CacheName template, final Callable<R> callable, final boolean direct, final Object... keys) {
        final Cache cache = getCache(appId, template);
        final ExecutorService executor = (direct ? ExecutorFactory.getDirectExecutor() : ExecutorFactory.getInstance());
        final DistributedExecutorService des = new DefaultExecutorService(cache, executor);
        final DistributedTask<R> task = toTask(des, callable);
        return des.submit(task, keys);
    }

    private static DistributedExecutorService getDistributedExecutorService(String appId) {
        final Cache cache = getCache(appId, CacheName.DIST);
        final ExecutorService executor = ExecutorFactory.getInstance();
        return new DefaultExecutorService(cache, executor);
    }

    private static <T> DistributedTask<T> toTask(DistributedExecutorService des, Callable<T> callable) {
        final Callable<T> wrapper = toTCCL(callable);
        final DistributedTaskBuilder<T> builder = des.createDistributedTaskBuilder(wrapper);
        return builder.build();
    }

    private static <R> Callable<R> toTCCL(Callable<R> callable) {
        return new TCCLCallable<R>(Wrappers.distribute(callable));
    }

    /**
     * Get cache.
     */
    public static <K, V> Cache<K, V> getCache(String appId, CacheName template) {
        if (template == null)
            throw new IllegalArgumentException("Null template!");

        return getCache(template, appId);
    }

    /**
     * Submit the task to distributed execution env, expecting result at the end.
     */
    public static <R> R submit(final String appId, final CacheName template, final Callable<R> task, Object... keys) {
        final Future<R> result = distribute(appId, template, task, true, keys);
        return Utils.quietGet(result);
    }

    /**
     * Submit the task to distributed execution env, it could be a fire-n-forget way.
     */
    public static <R> Future<R> fire(final String appId, final CacheName template, final Callable<R> task, Object... keys) {
        return distribute(appId, template, task, false, keys);
    }

    /**
     * Submit to single node.
     */
    public static <T> Future<T> single(final String appId, Callable<T> callable, Address toAddress) {
        final DistributedExecutorService des = getDistributedExecutorService(appId);
        final DistributedTask<T> task = toTask(des, callable);
        return des.submit(toAddress, task);
    }

    /**
     * Submit to all nodes.
     */
    public static <T> List<Future<T>> everywhere(final String appId, Callable<T> callable) {
        final DistributedExecutorService des = getDistributedExecutorService(appId);
        final DistributedTask<T> task = toTask(des, callable);
        return des.submitEverywhere(task);
    }

    public static GridFilesystem getGridFilesystem(String appId) {
        final ComponentRegistry registry = ComponentRegistry.getInstance();
        final Key<GridFilesystem> key = new SimpleKey<GridFilesystem>(appId, GridFilesystem.class);

        GridFilesystem gfs = registry.getComponent(key);
        if (gfs == null) {
            synchronized (InfinispanUtils.class) {
                gfs = registry.getComponent(key);
                if (gfs == null) {
                    final Cache<String, byte[]> data = getCache(appId, CacheName.DATA);
                    final Cache<String, GridFile.Metadata> metadata = getCache(appId, CacheName.METADATA);
                    gfs = new GridFilesystem(data, metadata);
                    registry.setComponent(key, gfs);
                }
            }
        }
        return gfs;
    }

    public static Address getLocalNode(String appId) {
        return getCache(appId, CacheName.DEFAULT).getAdvancedCache().getRpcManager().getAddress();
    }
}
