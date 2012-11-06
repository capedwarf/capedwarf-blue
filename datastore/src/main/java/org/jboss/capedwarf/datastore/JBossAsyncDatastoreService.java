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

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.threads.DirectFuture;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

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
        final TransactionWrapper tw = JBossTransaction.getTxWrapper(transaction);
        final Future<T> wrap = wrap(new Callable<T>() {
            public T call() throws Exception {
                JBossTransaction.attach(tw);
                try {
                    return callable.call();
                } finally {
                    JBossTransaction.detach(tw);
                }
            }
        });
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

        getDatastoreCallbacks().executePreGetCallbacks(JBossAsyncDatastoreService.this, Lists.newArrayList(keyIterable), map);

        final List<Key> requiredKeys = Lists.newArrayList(keyIterable);
        if (map.isEmpty() == false) {
            requiredKeys.removeAll(map.keySet()); // remove manually added keys
        }

        final TransactionWrapper tw = JBossTransaction.getTxWrapper(transaction);
        final Future<Map<Key, Entity>> wrap = wrap(new Callable<Map<Key, Entity>>() {
            public Map<Key, Entity> call() throws Exception {
                JBossTransaction.attach(tw);
                try {
                    for (Key key : requiredKeys) {
                        final Entity entity = getDelegate().get(transaction, key);
                        if (entity != null) {
                            map.put(key, entity);
                        }
                    }
                    return map;
                } finally {
                    JBossTransaction.detach(tw);
                }
            }
        });
        return new PostFuture<Map<Key,Entity>>(wrap) {
            protected void after(Map<Key, Entity> result) {
                getDatastoreCallbacks().executePostLoadCallbacks(postTxProvider(transaction), Lists.newArrayList(map.values()));
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
        return doPut(transaction, entity, false);
    }

    public Future<List<Key>> put(final Transaction transaction, final Iterable<Entity> entityIterable) {
        getDatastoreCallbacks().executePrePutCallbacks(JBossAsyncDatastoreService.this, Lists.newArrayList(entityIterable));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostPutCallbacks(postTxProvider(transaction), Lists.newArrayList(entityIterable));
            }
        };

        final TransactionWrapper tw = JBossTransaction.getTxWrapper(transaction);
        final Future<List<Key>> wrap = wrap(new Callable<List<Key>>() {
            public List<Key> call() throws Exception {
                JBossTransaction.attach(tw);
                try {
                    final List<Key> keys = new ArrayList<Key>();
                    for (Entity entity : entityIterable) {
                        keys.add(getDelegate().put(transaction, entity, null)); // do not post, until get is called
                    }
                    return keys;
                } finally {
                    JBossTransaction.detach(tw);
                }
            }
        });
        return handleGetWithPost(wrap, JBossTransaction.getTx(), post);
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
        getDatastoreCallbacks().executePreDeleteCallbacks(JBossAsyncDatastoreService.this, Lists.newArrayList(keyIterable));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostDeleteCallbacks(postTxProvider(transaction), Lists.newArrayList(keyIterable));
            }
        };

        final TransactionWrapper tw = JBossTransaction.getTxWrapper(transaction);
        final Future<Void> wrap = wrap(new Callable<Void>() {
            public Void call() throws Exception {
                JBossTransaction.attach(tw);
                try {
                    for (Key key : keyIterable) {
                        getDelegate().delete(transaction, key, null); // do not delete until get is called
                    }
                    return null;
                } finally {
                    JBossTransaction.detach(tw);
                }
            }
        });
        return handleGetWithPost(wrap, JBossTransaction.getTx(), post);
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
