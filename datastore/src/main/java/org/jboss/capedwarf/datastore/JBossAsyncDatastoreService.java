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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DeleteContext;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreGetContext;
import com.google.appengine.api.datastore.PutContext;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.threads.DirectFuture;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.common.threads.FutureGetDelegate;

/**
 * JBoss async DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossAsyncDatastoreService extends AbstractDatastoreService implements AsyncDatastoreService {

    public JBossAsyncDatastoreService() {
        this(null);
    }

    public JBossAsyncDatastoreService(DatastoreServiceConfig config) {
        super(new DatastoreServiceImpl(config));
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

    protected <T> Future<T> wrap(final Transaction transaction, final Callable<T> callable, final Runnable pre, final Function<T, Void> post) {
        if (pre != null) {
            pre.run();
        }
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        final Future<T> wrap = wrap(new Callable<T>() {
            public T call() throws Exception {
                if (tx != null) {
                    JBossTransaction.resumeTx(tx);
                }
                try {
                    return callable.call();
                } finally {
                    if (tx != null) {
                        JBossTransaction.suspendTx();
                    }
                }
            }
        });
        return new FutureGetDelegate<T>(wrap) {
            public T get() throws InterruptedException, ExecutionException {
                final T result = wrap.get();
                if (post != null) {
                    post.apply(result);
                }
                return result;
            }

            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                final T result = wrap.get(timeout, unit);
                if (post != null) {
                    post.apply(result);
                }
                return result;
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

    public Future<Entity> get(final Key key) {
        return get(getCurrentTransaction(null), key);
    }

    public Future<Entity> get(Transaction transaction, Key key) {
        return doGet(transaction, key);
    }

    public Future<Map<Key, Entity>> get(final Iterable<Key> keyIterable) {
        return get(getCurrentTransaction(null), keyIterable);
    }

    public Future<Map<Key, Entity>> get(final Transaction transaction, final Iterable<Key> keyIterable) {
        final Map<Key, Entity> map = new LinkedHashMap<Key, Entity>();
        final PreGetContext preGetContext = DatastoreCallbacks.createPreGetContext(this, Lists.newArrayList(keyIterable), map);
        final Function<Key, Void> pre = new Function<Key, Void>() {
            public Void apply(Key input) {
                getDatastoreCallbacks().executePreGetCallbacks(preGetContext);
                return null;
            }
        };
        final List<Entity> results = new ArrayList<Entity>();
        final Function<Map.Entry<Key, Entity>, Void> post = new Function<Map.Entry<Key, Entity>, Void>() {
            public Void apply(Map.Entry<Key, Entity> input) {
                getDatastoreCallbacks().executePostLoadCallbacks(DatastoreCallbacks.createPostLoadContext(postTxProvider(transaction), results));
                return null;
            }
        };
        for (Key key : keyIterable) {
            pre.apply(key);
        }
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        final Future<Map<Key, Entity>> wrap = wrap(new Callable<Map<Key, Entity>>() {
            public Map<Key, Entity> call() throws Exception {
                if (tx != null) {
                    JBossTransaction.resumeTx(tx);
                }
                try {
                    for (Key key : keyIterable) {
                        Entity previous = map.get(key);
                        if (previous == null) {
                            final Entity entity = getDelegate().get(transaction, key);
                            if (entity != null) {
                                map.put(key, entity);
                                previous = entity;
                            }
                        }
                        if (previous != null) {
                            results.add(previous);
                        }
                    }
                    return map;
                } finally {
                    if (tx != null) {
                        JBossTransaction.suspendTx();
                    }
                }
            }
        });
        return new FutureGetDelegate<Map<Key, Entity>>(wrap) {
            public Map<Key, Entity> get() throws InterruptedException, ExecutionException {
                wrap.get();
                for (Map.Entry<Key, Entity> entry : map.entrySet()) {
                    post.apply(entry);
                }
                return map;
            }

            public Map<Key, Entity> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                wrap.get(timeout, unit);
                for (Map.Entry<Key, Entity> entry : map.entrySet()) {
                    post.apply(entry);
                }
                return map;
            }
        };
    }

    public Future<Key> put(final Entity entity) {
        return put(getCurrentTransaction(null), entity);
    }

    public Future<List<Key>> put(final Iterable<Entity> entityIterable) {
        return put(getCurrentTransaction(null), entityIterable);
    }

    public Future<Key> put(Transaction transaction, Entity entity) {
        return doPut(transaction, entity);
    }

    public Future<List<Key>> put(final Transaction transaction, final Iterable<Entity> entityIterable) {
        final PutContext preContext = DatastoreCallbacks.createPutContext(this, Lists.newArrayList(entityIterable));
        final Function<Entity, Void> pre = new Function<Entity, Void>() {
            public Void apply(Entity input) {
                getDatastoreCallbacks().executePrePutCallbacks(preContext);
                return null;
            }
        };
        final PutContext postContext = DatastoreCallbacks.createPutContext(postTxProvider(transaction), Lists.newArrayList(entityIterable));
        final Function<Entity, Void> post = new Function<Entity, Void>() {
            public Void apply(Entity input) {
                getDatastoreCallbacks().executePostPutCallbacks(postContext);
                return null;
            }
        };
        for (Entity entity : entityIterable) {
            pre.apply(entity);
        }
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        final Future<List<Key>> wrap = wrap(new Callable<List<Key>>() {
            public List<Key> call() throws Exception {
                if (tx != null) {
                    JBossTransaction.resumeTx(tx);
                }
                try {
                    final List<Key> keys = new ArrayList<Key>();
                    for (Entity entity : entityIterable) {
                        keys.add(getDelegate().put(transaction, entity));
                    }
                    return keys;
                } finally {
                    if (tx != null) {
                        JBossTransaction.suspendTx();
                    }
                }
            }
        });
        return new FutureGetDelegate<List<Key>>(wrap) {
            public List<Key> get() throws InterruptedException, ExecutionException {
                final List<Key> result = wrap.get();
                for (Entity entity : entityIterable) {
                    post.apply(entity);
                }
                return result;
            }

            public List<Key> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                final List<Key> result = wrap.get(timeout, unit);
                for (Entity entity : entityIterable) {
                    post.apply(entity);
                }
                return result;
            }
        };
    }

    public Future<Void> delete(final Key... keys) {
        return delete(getCurrentTransaction(null), keys);
    }

    public Future<Void> delete(final Transaction transaction, final Key... keys) {
        return delete(transaction, Arrays.asList(keys));
    }

    public Future<Void> delete(final Iterable<Key> keyIterable) {
        return delete(getCurrentTransaction(null), keyIterable);
    }

    public Future<Void> delete(final Transaction transaction, final Iterable<Key> keyIterable) {
        final DeleteContext preContext = DatastoreCallbacks.createDeleteContext(this, Lists.newArrayList(keyIterable));
        final Function<Key, Void> pre = new Function<Key, Void>() {
            public Void apply(Key input) {
                getDatastoreCallbacks().executePreDeleteCallbacks(preContext);
                return null;
            }
        };
        final DeleteContext postContext = DatastoreCallbacks.createDeleteContext(postTxProvider(transaction), Lists.newArrayList(keyIterable));
        final Function<Key, Void> post = new Function<Key, Void>() {
            public Void apply(Key input) {
                getDatastoreCallbacks().executePostDeleteCallbacks(postContext);
                return null;
            }
        };
        for (Key key : keyIterable) {
            pre.apply(key);
        }
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        final Future<Void> wrap = wrap(new Callable<Void>() {
            public Void call() throws Exception {
                if (tx != null) {
                    JBossTransaction.resumeTx(tx);
                }
                try {
                    for (Key key : keyIterable) {
                        getDelegate().delete(transaction, key);
                    }
                    return null;
                } finally {
                    if (tx != null) {
                        JBossTransaction.suspendTx();
                    }
                }
            }
        });
        return new FutureGetDelegate<Void>(wrap) {
            public Void get() throws InterruptedException, ExecutionException {
                wrap.get();
                for (Key key : keyIterable) {
                    post.apply(key);
                }
                return null;
            }

            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                wrap.get(timeout, unit);
                for (Key key : keyIterable) {
                    post.apply(key);
                }
                return null;
            }
        };
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

    /**
     * Testing only!
     */
    public void clearCache() {
        getDelegate().clearCache();
    }
}
