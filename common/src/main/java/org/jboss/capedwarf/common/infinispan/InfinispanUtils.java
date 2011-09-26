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

import org.infinispan.Cache;
import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InfinispanUtils {
    private static String[] defaultJndiNames = {"java:jboss/infinispan/capedwarf", "java:CacheManager/capedwarf"};
    private static final String DATA = "GridFilesystem_DATA";
    private static final String METADATA = "GridFilesystem_METADATA";

    private static volatile GridFilesystem gridFilesystem;

    public static EmbeddedCacheManager getCacheManager() {
        return JndiLookupUtils.lazyLookup("infinispan.jndi.name", EmbeddedCacheManager.class, defaultJndiNames);
    }

    public static GridFilesystem getGridFilesystem() {
        if (gridFilesystem == null) {
            synchronized (InfinispanUtils.class) {
                if (gridFilesystem == null) {
                    EmbeddedCacheManager cm = getCacheManager();
                    Cache<String, byte[]> data = cm.getCache(DATA);
                    Cache<String, GridFile.Metadata> metadata = cm.getCache(METADATA);
                    data.start();
                    metadata.start();
                    gridFilesystem = new GridFilesystem(data, metadata);
                }
            }
        }
        return gridFilesystem;
    }
}
