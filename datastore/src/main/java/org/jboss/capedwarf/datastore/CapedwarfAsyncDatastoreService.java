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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.utils.FutureWrapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.async.Wrappers;
import org.jboss.capedwarf.common.threads.DirectFuture;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

/**
 * JBoss async DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class CapedwarfAsyncDatastoreService extends AbstractDatastoreService implements ExposedAsyncDatastoreService {

    public CapedwarfAsyncDatastoreService() {
        this(null);
    }

    public CapedwarfAsyncDatastoreService(DatastoreServiceConfig config) {
        super(DatastoreServiceImpl.async(config));
    }

    protected static <T> Future<T> wrap(final Callable<T> callable) {
        return ExecutorFactory.wrap(callable);
    }

    protected <T> Future<T> tx(final Callable<T> callable) {
        return wrap(getCurrentTransaction(null), callable, null, null);
    }

    protected CurrentTransactionProvider postTxProvider(final Transaction tx) {
        return new CurrentTransactionProvider() {
            public Transaction getCurrentTransaction(Transaction transaction) {
                return (tx != null) ? tx : transaction;
            }
        };
    }

    protected <T> Callable<T> env(final Callable<T> callable) {
        return Wrappers.wrap(callable);
    }

    protected <T> Future<T> wrap(final Transaction transaction, final Callable<T> callable, final Runnable pre, final Function<T, Void> post) {
        if (pre != null) {
            pre.run();
        }
        final TransactionWrapper tw = CapedwarfTransaction.getTxWrapper(transaction);
        final Future<T> wrap = wrap(env(new Callable<T>() {
            public T call() throws Exception {
                CapedwarfTransaction.attach(tw);
                try {
                    return callable.call();
                } finally {
                    CapedwarfTransaction.detach(tw);
                }
            }
        }));
        return new PostFuture<T>(wrap) {
            protected void after(T result) {
                if (post != null) {
                    post.apply(result);
                }
            }
        };
    }

    public Future<Transaction> beginTransaction() {
        return beginTransaction(TransactionOptions.Builder.withDefaults());
    }

    public Future<Transaction> beginTransaction(final TransactionOptions transactionOptions) {
        return DirectFuture.create(new Callable<Transaction>() {
            public Transaction call() throws Exception {
                return getDelegate().beginTransaction(transactionOptions);
            }
        });
    }

    @TxTask
    public Future<Entity> get(final Key key) {
        return get(getCurrentTransaction(null), key);
    }

    @TxTask
    public Future<Entity> get(Transaction transaction, final Key key) {
        Future<Entity> future = doGet(transaction, key);
        return new FutureWrapper<Entity, Entity>(future) {
            protected Entity wrap(Entity entity) throws Exception {
                if (entity == null)
                    throw new EntityNotFoundException(key);
                return entity;
            }

            protected Throwable convertException(Throwable throwable) {
                return throwable;
            }
        };
    }

    @TxTask
    public Future<Map<Key, Entity>> get(final Iterable<Key> keyIterable) {
        return get(getCurrentTransaction(null), keyIterable);
    }

    @TxTask
    public Future<Map<Key, Entity>> get(final Transaction transaction, final Iterable<Key> keyIterable) {
        final Map<Key, Entity> map = new LinkedHashMap<Key, Entity>();

        getDatastoreCallbacks().executePreGetCallbacks(this, Lists.newArrayList(keyIterable), map);

        final List<Key> requiredKeys = Lists.newArrayList(keyIterable);
        if (map.isEmpty() == false) {
            requiredKeys.removeAll(map.keySet()); // remove manually added keys
        }

        final TransactionWrapper tw = CapedwarfTransaction.getTxWrapper(transaction);
        final Future<Map<Key, Entity>> wrap = wrap(env(new Callable<Map<Key, Entity>>() {
            public Map<Key, Entity> call() throws Exception {
                CapedwarfTransaction.attach(tw);
                try {
                    getDelegate().get(transaction, requiredKeys, map);
                    return map;
                } finally {
                    CapedwarfTransaction.detach(tw);
                }
            }
        }));
        return new PostFuture<Map<Key,Entity>>(wrap) {
            protected void after(Map<Key, Entity> result) {
                getDatastoreCallbacks().executePostLoadCallbacks(postTxProvider(transaction), Lists.newArrayList(map.values()));
            }
        };
    }

    @TxTask
    public Future<Key> put(final Entity entity) {
        return put(getCurrentTransaction(null), entity);
    }

    @TxTask
    public Future<List<Key>> put(final Iterable<Entity> entityIterable) {
        return put(getCurrentTransaction(null), entityIterable);
    }

    @TxTask
    public Future<Key> put(Transaction transaction, Entity entity) {
        return doPut(transaction, entity, false);
    }

    @TxTask
    public Future<List<Key>> put(final Transaction transaction, final Iterable<Entity> entityIterable) {
        getDatastoreCallbacks().executePrePutCallbacks(this, Lists.newArrayList(entityIterable));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostPutCallbacks(postTxProvider(transaction), Lists.newArrayList(entityIterable));
            }
        };

        final TransactionWrapper tw = CapedwarfTransaction.getTxWrapper(transaction);
        final Future<List<Key>> wrap = wrap(env(new Callable<List<Key>>() {
            public List<Key> call() throws Exception {
                CapedwarfTransaction.attach(tw);
                try {
                    final List<Key> keys = new ArrayList<Key>();
                    keys.addAll(getDelegate().put(transaction, entityIterable, null)); // do not post, until get is called
                    return keys;
                } finally {
                    CapedwarfTransaction.detach(tw);
                }
            }
        }));
        return handleGetWithPost(wrap, CapedwarfTransaction.getTx(), post);
    }

    @TxTask
    public Future<Void> delete(final Key... keys) {
        return delete(getCurrentTransaction(null), keys);
    }

    @TxTask
    public Future<Void> delete(final Transaction transaction, final Key... keys) {
        return delete(transaction, Arrays.asList(keys));
    }

    @TxTask
    public Future<Void> delete(final Iterable<Key> keyIterable) {
        return delete(getCurrentTransaction(null), keyIterable);
    }

    @TxTask
    public Future<Void> delete(final Transaction transaction, final Iterable<Key> keyIterable) {
        getDatastoreCallbacks().executePreDeleteCallbacks(this, Lists.newArrayList(keyIterable));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostDeleteCallbacks(postTxProvider(transaction), Lists.newArrayList(keyIterable));
            }
        };

        final TransactionWrapper tw = CapedwarfTransaction.getTxWrapper(transaction);
        final Future<Void> wrap = wrap(env(new Callable<Void>() {
            public Void call() throws Exception {
                CapedwarfTransaction.attach(tw);
                try {
                    getDelegate().delete(transaction, keyIterable, null); // do not delete until get is called
                    return null;
                } finally {
                    CapedwarfTransaction.detach(tw);
                }
            }
        }));
        return handleGetWithPost(wrap, CapedwarfTransaction.getTx(), post);
    }

    public Future<KeyRange> allocateIds(final String s, final long l) {
        return allocateIds(null, s, l);
    }

    public Future<KeyRange> allocateIds(final Key key, final String s, final long l) {
        return tx(new Callable<KeyRange>() {
            public KeyRange call() throws Exception {
                return getDelegate().allocateIds(key, s, l);
            }
        });
    }

    public Future<DatastoreAttributes> getDatastoreAttributes() {
        return wrap(new Callable<DatastoreAttributes>() {
            public DatastoreAttributes call() throws Exception {
                return getDelegate().getDatastoreAttributes();
            }
        });
    }

    public Future<Map<Index, Index.IndexState>> getIndexes() {
        return wrap(new Callable<Map<Index, Index.IndexState>>() {
            public Map<Index, Index.IndexState> call() throws Exception {
                return getDelegate().getIndexes();
            }
        });
    }
}
