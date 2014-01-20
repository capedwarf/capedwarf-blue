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

import java.util.HashSet;
import java.util.Set;

import org.infinispan.notifications.Listenable;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Key;
import org.jboss.capedwarf.shared.components.SetKey;
import org.jboss.capedwarf.shared.components.Slot;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CacheListenerRegistry {
    /**
     * Register listener at runtime.
     *
     * @param handle the cache handle
     */
    public static synchronized void registerListener(Listenable listenable, CacheListenerHandle handle) {
        final Key<Set<CacheListenerHandle>> key = new SetKey<CacheListenerHandle>(AppIdFactory.getAppId(), Slot.CACHE_LISTENERS);
        ComponentRegistry registry = ComponentRegistry.getInstance();
        Set<CacheListenerHandle> handles = registry.getComponent(key);
        if (handles == null || handles.contains(handle) == false) {
            if (handles == null) {
                handles = new HashSet<CacheListenerHandle>();
                registry.setComponent(key, handles);
            }
            handles.add(handle);

            final ClassLoader cl = Application.getAppClassLoader();
            listenable.addListener(handle.createListener(cl));
        }
    }
}
