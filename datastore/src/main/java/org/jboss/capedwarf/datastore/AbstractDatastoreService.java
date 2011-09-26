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

package org.jboss.capedwarf.datastore;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Base Datastore service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractDatastoreService implements BaseDatastoreService {
    protected Logger log = Logger.getLogger(getClass().getName());
    protected Cache<Key, Entity> store = createStore();

    protected Cache<Key, Entity> createStore() {
        EmbeddedCacheManager manager = InfinispanUtils.getCacheManager();
        String appName = "DUMMY"; // TODO
        return manager.getCache(appName, true);
    }

    public PreparedQuery prepare(Query query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PreparedQuery prepare(Transaction transaction, Query query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Transaction getCurrentTransaction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Transaction getCurrentTransaction(Transaction transaction) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Transaction> getActiveTransactions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
