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

import java.util.List;
import java.util.Set;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Namespaces service.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class NamespaceServiceImpl implements NamespaceServiceInternal {
    private static final Function<Entity, String> FN = new Function<Entity, String>() {
        public String apply(Entity entity) {
            if (entity.getKey().getId() == Entities.NAMESPACE_METADATA_EMPTY_ID) {
                return "";
            } else {
                return entity.getKey().getName();
            }
        }
    };

    NamespaceServiceImpl() {
    }

    protected Set<String> getSet(String kind) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = ds.prepare(new Query(kind));
        List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        return Sets.newHashSet(Lists.transform(list, FN));
    }

    public Set<String> getNamespaces() {
        String oldNS = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return getSet(Entities.NAMESPACE_METADATA_KIND);
        } finally {
            NamespaceManager.set(oldNS);
        }
    }

    public Set<String> getKindsPerNamespace() {
        return getKindsPerNamespace(NamespaceManager.get());
    }

    public Set<String> getKindsPerNamespace(String namespace) {
        String oldNS = NamespaceManager.get();
        NamespaceManager.set(namespace);
        try {
            return getSet(Entities.KIND_METADATA_KIND);
        } finally {
            NamespaceManager.set(oldNS);
        }
    }

    public SetMultimap<String, String> getKindsPerNamespaces() {
        SetMultimap<String, String> multimap = HashMultimap.create();
        for (String namespace : getNamespaces()) {
            multimap.putAll(namespace, getKindsPerNamespace(namespace));
        }
        return multimap;
    }
}
