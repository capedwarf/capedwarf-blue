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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javassist.util.proxy.MethodHandler;
import org.hibernate.search.Environment;
import org.hibernate.search.backend.impl.jgroups.JGroupsChannelProvider;
import org.hibernate.search.cfg.EntityMapping;
import org.hibernate.search.cfg.SearchMapping;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IndexingConfigurationBuilder;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;
import org.jboss.capedwarf.common.reflection.BytecodeUtils;
import org.jgroups.JChannel;
import org.jgroups.Receiver;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InfinispanUtils {
    private static String[] defaultJndiNames = {"java:jboss/infinispan/container/capedwarf"};

    private static volatile int cacheManagerUsers;
    private static EmbeddedCacheManager cacheManager;
    private static final Map<String, GridFilesystem> gridFilesystems = new HashMap<String, GridFilesystem>();

    protected static EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }

    protected static <K, V> Cache<K, V> getCache(CacheName config, String appId, ConfigurationCallback callback) {
        final String cacheName = toCacheName(config, appId);
        //noinspection SynchronizeOnNonFinalField
        synchronized (cacheManager) {
            final Cache<K, V> cache = cacheManager.getCache(toCacheName(config, appId), false);
            if (cache != null)
                return cache;

            final ConfigurationBuilder builder = callback.configure(cacheManager);
            if (builder != null) {
                cacheManager.defineConfiguration(cacheName, builder.build());
            }

            return cacheManager.getCache(cacheName, true);
        }
    }

    protected static String toCacheName(CacheName config, String appId) {
        return config.getName() + "_" + appId;
    }

    protected static JChannel wrap(final JChannel channel) {
        final MethodHandler handler = new MethodHandler() {
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                final String methodName = thisMethod.getName();
                if ("setReceiver".equals(methodName) && args.length == 1 && (args[0] instanceof Receiver)) {
                    final Receiver passed = (Receiver) args[0];
                    channel.setReceiver(new WrapperReceiver(passed));
                    return null;
                } else {
                    return thisMethod.invoke(channel, args);
                }
            }
        };
        return BytecodeUtils.proxy(JChannel.class, handler);
    }

    public static Configuration getConfiguration(CacheName config) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        if (config == null)
            throw new IllegalArgumentException("Null config enum!");

        final Configuration c = cacheManager.getCacheConfiguration(config.getName());
        if (c == null)
            throw new IllegalArgumentException("No such default cache config: " + config);

        return c;
    }

    public static SearchMapping applyIndexing(CacheName config, ConfigurationBuilder builder, Class<?> ... classes) {
        final String appId = Application.getAppId();
        final IndexingConfigurationBuilder indexing = builder.indexing();
        indexing.addProperty("hibernate.search.default.indexBase", "./indexes_" + appId);
        final SearchMapping mapping = new SearchMapping();
        for (Class<?> clazz : classes) {
            final EntityMapping entity = mapping.entity(clazz);
            entity.indexed().indexName(toCacheName(config, appId) + "__" + clazz.getName());
        }
        indexing.setProperty(Environment.MODEL_MAPPING, mapping);

        final JChannel channel = JndiLookupUtils.lookup("infinispan.indexing.channel", JChannel.class, "java:jboss/capedwarf/indexing/channel");
        indexing.setProperty(JGroupsChannelProvider.CHANNEL_INJECT, wrap(channel));

        return mapping;
    }

    public static <K, V> Cache<K, V> getCache(CacheName config) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        if (config == null)
            throw new IllegalArgumentException("Null config enum!");
        if (config.hasConfig())
            throw new IllegalArgumentException("Cache " + config + " needs custom configuration!");

        final String appId = Application.getAppId();
        //noinspection SynchronizeOnNonFinalField
        synchronized (cacheManager) {
            final Cache<K, V> cache = cacheManager.getCache(toCacheName(config, appId), false);
            if (cache != null)
                return cache;
        }

        final Configuration existing = getConfiguration(config);
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.read(existing);

        final ConfigurationCallback callback = new ConfigurationCallback() {
            public ConfigurationBuilder configure(EmbeddedCacheManager manager) {
                return builder;
            }
        };

        return getCache(config, appId, callback);
    }
    
    public static <K, V> Cache<K, V> getCache(CacheName config, ConfigurationCallback callback) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        if (config == null)
            throw new IllegalArgumentException("Null config enum!");
        if (config.hasConfig() == false)
            throw new IllegalArgumentException("Cache " + config + " has default configuration!");

        final String appId = Application.getAppId();
        return getCache(config, appId, callback);
    }

    public static <R> R submit(final CacheName config, final Callable<R> task, Object... keys) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        
        final Cache cache = getCache(config);
        try {
            final DistributedExecutorService des = new DefaultExecutorService(cache);
            final Future<R> result = des.submit(task, keys);
            return result.get();
        } catch (Exception e) {
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }        
    }
    
    public static GridFilesystem getGridFilesystem() {
        final String appId = Application.getAppId();
        GridFilesystem gfs = gridFilesystems.get(appId);
        if (gfs == null) {
            synchronized (gridFilesystems) {
                gfs = gridFilesystems.get(appId);
                if (gfs == null) {
                    final Cache<String, byte[]> data = getCache(CacheName.DATA);
                    final Cache<String, GridFile.Metadata> metadata = getCache(CacheName.METADATA);
                    gfs = new GridFilesystem(data, metadata);
                    gridFilesystems.put(appId, gfs);
                }
            }
        }
        return gfs;
    }

    public static synchronized void initApplicationData(String appId) {
        if (cacheManager == null) {
            cacheManager = JndiLookupUtils.lazyLookup("infinispan.jndi.name", EmbeddedCacheManager.class, defaultJndiNames);
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
}
