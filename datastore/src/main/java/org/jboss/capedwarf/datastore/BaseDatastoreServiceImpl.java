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

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.apphosting.api.ApiProxy;
import org.hibernate.search.query.engine.spi.TimeoutExceptionFactory;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.metadata.Metadata;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.spi.SearchManagerImplementor;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.reflection.FieldInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.datastore.query.Indexes;
import org.jboss.capedwarf.datastore.query.PreparedQueryImpl;
import org.jboss.capedwarf.datastore.query.QueryConverter;
import org.jboss.capedwarf.datastore.query.QueryHandleService;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.config.IndexesXml;

/**
 * Base Datastore service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class BaseDatastoreServiceImpl implements BaseDatastoreService, CurrentTransactionProvider, PostLoadHandle, QueryHandleService {
    private static final FieldInvocation<Long> VERSION = ReflectionUtils.cacheField("org.infinispan.container.versioning.SimpleClusteredVersion", "version");
    private static final Map<ClassLoader, DatastoreServiceConfig> configs = new WeakHashMap<ClassLoader, DatastoreServiceConfig>();

    protected final Logger log = Logger.getLogger(getClass().getName());
    protected final String appId;
    protected final AdvancedCache<Key, Entity> store;
    protected final AdvancedCache<Key, Entity> ignoreReturnStore;
    protected final SearchManager searchManager;
    private final QueryConverter queryConverter;
    private DatastoreServiceConfig config;
    private volatile DatastoreCallbacks datastoreCallbacks;
    private final QueryTypeFactories factories;
    private final AdvancedCache<Key, EntityGroupMetadata> entityGroupMetadataStore;

    /**
     * Cache default config.
     * No need to parse potential callbacks on every new default instance.
     *
     * @param cl the app classloader
     * @return config
     */
    private static synchronized DatastoreServiceConfig withDefaults(ClassLoader cl) {
        DatastoreServiceConfig dsc = configs.get(cl);
        if (dsc == null) {
            dsc = DatastoreServiceConfig.Builder.withDefaults();
            configs.put(cl, dsc);
        }
        return dsc;
    }

    public BaseDatastoreServiceImpl() {
        this(null);
    }

    public BaseDatastoreServiceImpl(DatastoreServiceConfig config) {
        this.appId = Application.getAppId();
        final ClassLoader classLoader = getAppClassLoader();
        this.config = (config == null ? withDefaults(classLoader) : config);

        store = createStore().getAdvancedCache().with(classLoader);

        // we don't expect "put", "remove" to return anything
        ignoreReturnStore = store.withFlags(Flag.IGNORE_RETURN_VALUES);

        Compatibility c = CompatibilityUtils.getInstance();
        boolean useMetadata = (c.isEnabled(Compatibility.Feature.DISABLE_METADATA) == false);
        if (useMetadata) {
            entityGroupMetadataStore = InfinispanUtils.<Key, EntityGroupMetadata>getCache(appId, CacheName.DATASTORE_VERSIONS)
                    .getAdvancedCache()
                    .with(classLoader)
                    .withFlags(Flag.IGNORE_RETURN_VALUES);
        } else {
            entityGroupMetadataStore = null;
        }

        this.searchManager = Search.getSearchManager(store);
        if (searchManager instanceof SearchManagerImplementor) {
            SearchManagerImplementor smi = (SearchManagerImplementor) searchManager;
            smi.setTimeoutExceptionFactory(new TimeoutExceptionFactory() {
                public RuntimeException createTimeoutException(String message, org.apache.lucene.search.Query query) {
                    return new ApiProxy.ApiDeadlineExceededException("datastore", "RunQuery");
                }
            });
        }
        this.queryConverter = new QueryConverter(searchManager);
        this.factories = new QueryTypeFactories(this);
    }

    protected ClassLoader getAppClassLoader() {
        return Application.getAppClassLoader();
    }

    protected Cache<Key, Entity> createStore() {
        return InfinispanUtils.getCache(appId, CacheName.DEFAULT);
    }

    public DatastoreCallbacks getDatastoreCallbacks() {
        if (datastoreCallbacks == null) {
            Object callbacks = ReflectionUtils.invokeInstanceMethod(getDatastoreServiceConfig(), "getDatastoreCallbacks");
            datastoreCallbacks = new DatastoreCallbacks(callbacks);
        }
        return datastoreCallbacks;
    }

    protected final void putEntityGroupKey(Key key) {
        if (entityGroupMetadataStore != null) {
            entityGroupMetadataStore.put(Entities.createEntityGroupKey(key), EntityGroupMetadata.SINGLETON);
        }
    }

    protected final Entity getEntityGroupMetadataEntity(Key key) {
        if (entityGroupMetadataStore != null) {
            Entity entity = new Entity(key);
            entity.setProperty(Entity.VERSION_RESERVED_PROPERTY, readEntityGroupVersion(key));
            return entity;
        } else {
            throw new IllegalStateException("Metadata is disabled, enable it via compatibility property: " + Compatibility.Feature.DISABLE_METADATA);
        }
    }

    private Long readEntityGroupVersion(Key key) {
        CacheEntry cacheEntry = entityGroupMetadataStore.getCacheEntry(key);
        Metadata metadata = cacheEntry.getMetadata();
        return VERSION.invoke(metadata.version());
    }

    public void execute(Entity result) {
        getDatastoreCallbacks().executePostLoadCallbacks(this, result);
    }

    public Cache<Key, Entity> getCache() {
        return store;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public PreparedQuery createQuery(Transaction tx, Query query) {
        if (tx != null && query.getAncestor() == null) {
            throw new IllegalArgumentException("Only ancestor queries are allowed inside transactions.");
        }
        javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            IndexesXml.Index index = Indexes.getIndex(query);
            CacheQuery cacheQuery = queryConverter.convert(query, index);

            Double deadlineSeconds = getDatastoreServiceConfig().getDeadline();
            if (deadlineSeconds != null) {
                long deadlineMicroseconds = (long) (deadlineSeconds * 1000000);
                cacheQuery.timeout(deadlineMicroseconds, TimeUnit.MICROSECONDS);
            }

            return new PreparedQueryImpl(this, query, index, cacheQuery, tx != null);
        } finally {
            afterTx(transaction);
        }
    }

    public PreparedQuery prepare(Query query) {
        return prepare(null, query);
    }

    public PreparedQuery prepare(Transaction tx, Query query) {
        return factories.prepare(tx, query);
    }

    public Transaction getCurrentTransaction() {
        Transaction tx = CapedwarfTransaction.currentTransaction();
        if (tx == null)
            throw new NoSuchElementException("No current transaction.");

        return tx;
    }

    public Transaction getCurrentTransaction(Transaction transaction) {
        Transaction tx = CapedwarfTransaction.currentTransaction();
        return (tx != null) ? tx : transaction;
    }

    public Collection<Transaction> getActiveTransactions() {
        return CapedwarfTransaction.getTransactions();
    }

    static javax.transaction.Transaction beforeTx(Transaction tx) {
        if (tx == null) {
            // if tx is null, explicitly suspend current tx
            return CapedwarfTransaction.suspendTx();
        } else {
            if (!tx.isActive()) {
                throw new IllegalStateException("Transaction is not active: " + tx);
            }

            return null;
        }
    }

    static void afterTx(javax.transaction.Transaction transaction) {
        if (transaction != null) {
            CapedwarfTransaction.resumeTx(transaction);
        }
    }

    public DatastoreServiceConfig getDatastoreServiceConfig() {
        return config;
    }
}
