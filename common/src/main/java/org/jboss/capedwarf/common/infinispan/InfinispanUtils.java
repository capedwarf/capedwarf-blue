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

import com.google.apphosting.api.ApiProxy;
import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InfinispanUtils {
    private static String[] defaultJndiNames = {"java:jboss/infinispan/container/capedwarf", "java:CacheManager/capedwarf"};
    private static final String DATA = "GridFilesystem_DATA";
    private static final String METADATA = "GridFilesystem_METADATA";

    private static volatile int cacheManagerUsers;
    private static EmbeddedCacheManager cacheManager;
    private static final Map<String, GridFilesystem> gridFilesystems = new HashMap<String, GridFilesystem>();

    public static EmbeddedCacheManager getCacheManager() {
        return cacheManager;
    }

    public static <R> R submit(final CacheName cacheName, final Callable<R> task, Object... keys) {
        if (cacheManager == null)
            throw new IllegalArgumentException("CacheManager is null, should not be here?!");
        
        final Cache cache = cacheManager.getCache(cacheName.getName());
        try {
            final DistributedExecutorService des = new DefaultExecutorService(cache);
            final Future<R> result = des.submit(task, keys);
            return result.get();
        } catch (Exception e) {
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }        
    }
    
    public static GridFilesystem getGridFilesystem() {
        String appId = ApiProxy.getCurrentEnvironment().getAppId();
        GridFilesystem gfs = gridFilesystems.get(appId);
        if (gfs == null) {
            synchronized (gridFilesystems) {
                gfs = gridFilesystems.get(appId);
                if (gfs == null) {
                    EmbeddedCacheManager cm = getCacheManager();
                    Cache<String, byte[]> data = cm.getCache(DATA);
                    Cache<String, GridFile.Metadata> metadata = cm.getCache(METADATA);
                    data.start();
                    metadata.start();
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
