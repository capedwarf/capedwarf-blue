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

import com.google.appengine.api.datastore.*;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.datastore.query.PreparedQueryImpl;
import org.jboss.capedwarf.datastore.query.QueryConverter;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Base Datastore service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class AbstractDatastoreService implements BaseDatastoreService {
    protected final Logger log = Logger.getLogger(getClass().getName());
    protected final Cache<Key, Entity> store;
    protected final SearchManager searchManager;
    private final QueryConverter queryConverter;

    public AbstractDatastoreService() {
        this.store = createStore();
        this.searchManager = Search.getSearchManager(store);
        this.queryConverter = new QueryConverter(searchManager);
    }

    protected Cache<Key, Entity> createStore() {
        return getCache(getStoreCacheName());
    }

    private <K, V> Cache<K, V> getCache(String cacheName) {
        EmbeddedCacheManager manager = InfinispanUtils.getCacheManager();
        return manager.getCache(cacheName, true);
    }

    private String getStoreCacheName() {
        return "default"; // TODO
    }

    public PreparedQuery prepare(Query query) {
        CacheQuery cacheQuery = queryConverter.convert(query);
        return new PreparedQueryImpl(cacheQuery);
    }

    public PreparedQuery prepare(Transaction transaction, Query query) {
        return prepare(query);
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
