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

package org.jboss.capedwarf.datastore.metadata;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.infinispan.Cache;
import org.jboss.capedwarf.datastore.notifications.CacheListenerHandle;
import org.jboss.capedwarf.datastore.notifications.CacheListenerRegistry;
import org.jboss.capedwarf.datastore.query.QueryHandle;
import org.jboss.capedwarf.datastore.query.QueryHandleService;
import org.jboss.capedwarf.datastore.query.QueryTypeFactory;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * Metadata Query type factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MetadataQueryTypeFactory implements QueryTypeFactory {
    private static final CacheListenerHandle METADATA = new MetadataListener();

    private static final ThreadLocal<Boolean> FLAG = new ThreadLocal<Boolean>();

    public static QueryTypeFactory INSTANCE = new MetadataQueryTypeFactory();

    private static final Set<String> KINDS;

    static {
        KINDS = new HashSet<String>();
        KINDS.add(Entities.NAMESPACE_METADATA_KIND);
        KINDS.add(Entities.KIND_METADATA_KIND);
        KINDS.add(Entities.PROPERTY_METADATA_KIND);
    }

    private MetadataQueryTypeFactory() {
    }

    public static boolean isMetadataKind(String kind) {
        return KINDS.contains(kind);
    }

    public static boolean inProgress() {
        return (FLAG.get() != null);
    }

    public void initialize(QueryHandleService service) {
        if (Compatibility.getInstance().isEnabled(Compatibility.Feature.DISABLE_METADATA) == false) {
            Cache<Key, Entity> cache = service.getCache();
            // register namespaces listener
            CacheListenerRegistry.registerListener(cache, METADATA);
        }
    }

    public boolean handleQuery(Transaction tx, Query query) {
        return isMetadataKind(query.getKind());
    }

    public QueryHandle createQueryHandle(QueryHandleService service) {
        return service;
    }

    static void setFlag(boolean flag) {
        if (flag) FLAG.set(true);
        else FLAG.remove();
    }
}