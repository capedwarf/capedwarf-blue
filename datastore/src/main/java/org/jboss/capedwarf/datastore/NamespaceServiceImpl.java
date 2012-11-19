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
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.infinispan.Cache;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * Namespaces service.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class NamespaceServiceImpl implements NamespaceServiceInternal {
    NamespaceServiceImpl() {
    }

    public SetMultimap<String, String> getKindsPerNamespaces() {
        Cache<String, SetMultimap<String, String>> cache = InfinispanUtils.getCache(Application.getAppId(), CacheName.DIST);
        SetMultimap<String, String> result = cache.get(NAMESPACES);
        return (result != null) ? Multimaps.unmodifiableSetMultimap(result) : ImmutableSetMultimap.<String, String>of();
    }

    public Set<String> getKindsPerNamespace() {
        return getKindsPerNamespace(NamespaceManager.get());
    }

    public Set<String> getKindsPerNamespace(String namespace) {
        Set<String> kinds = getKindsPerNamespaces().get(namespace);
        return (kinds != null) ? Collections.unmodifiableSet(kinds) : Collections.<String>emptySet();
    }
}
