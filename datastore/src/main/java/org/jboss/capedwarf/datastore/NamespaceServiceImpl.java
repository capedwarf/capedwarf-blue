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

package org.jboss.capedwarf.datastore;

import java.util.Collections;
import java.util.Set;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entities;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.infinispan.Cache;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * Namespaces service.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class NamespaceServiceImpl implements NamespaceServiceInternal {
    NamespaceServiceImpl() {
    }

    protected Set<String> getCachedSet(String key) {
        Cache<String, Set<String>> cache = InfinispanUtils.getCache(Application.getAppId(), CacheName.DIST);
        Set<String> result = cache.get(key);
        return (result != null) ? Collections.unmodifiableSet(result) : Collections.<String>emptySet();
    }

    public Set<String> getNamespaces() {
        return getCachedSet(Entities.NAMESPACE_METADATA_KIND);
    }

    public Set<String> getKindsPerNamespace() {
        return getKindsPerNamespace(NamespaceManager.get());
    }

    public Set<String> getKindsPerNamespace(String namespace) {
        return getCachedSet(Entities.KIND_METADATA_KIND + namespace);
    }

    public SetMultimap<String, String> getKindsPerNamespaces() {
        SetMultimap<String, String> multimap = HashMultimap.create();
        for (String namespace : getNamespaces()) {
            multimap.putAll(namespace, getKindsPerNamespace(namespace));
        }
        return multimap;
    }
}
