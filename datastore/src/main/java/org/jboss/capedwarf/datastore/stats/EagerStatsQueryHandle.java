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

package org.jboss.capedwarf.datastore.stats;

import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.capedwarf.datastore.notifications.CacheListenerHandle;
import org.jboss.capedwarf.datastore.notifications.CacheListenerRegistry;
import org.jboss.capedwarf.datastore.query.AbstractQueryHandle;
import org.jboss.capedwarf.datastore.query.QueryHandleService;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * Eager query handle.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class EagerStatsQueryHandle extends AbstractQueryHandle {
    private static final CacheListenerHandle HANDLE = new EagerListenerHandle();

    EagerStatsQueryHandle(QueryHandleService service) {
        super(service);
        CacheListenerRegistry.registerListener(service.getCache(), HANDLE);
    }

    public PreparedQuery createQuery(Transaction tx, Query query) {
        return service.createQuery(tx, query); // just run the query
    }

    private static class EagerListenerHandle implements CacheListenerHandle {
        public Object createListener(ClassLoader cl) {
            Compatibility c = Compatibility.getInstance();
            String value = c.getValue(Compatibility.Feature.ENABLE_EAGER_DATASTORE_STATS);
            return "async".equals(value) ? new AsyncEagerListener() : new EagerListener();
        }
    }
}