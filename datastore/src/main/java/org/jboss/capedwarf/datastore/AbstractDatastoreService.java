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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Abstract base DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractDatastoreService implements BaseDatastoreService, CurrentTransactionProvider {
    private final DatastoreServiceInternal datastoreService;

    public AbstractDatastoreService(DatastoreServiceInternal datastoreService) {
        this.datastoreService = datastoreService;
    }

    protected DatastoreCallbacks getDatastoreCallbacks() {
        return getDelegate().getDatastoreCallbacks();
    }

    DatastoreServiceInternal getDelegate() {
        return datastoreService;
    }

    protected abstract CurrentTransactionProvider postTxProvider(final Transaction tx);

    protected abstract <T> Future<T> wrap(final Transaction transaction, final Callable<T> callable, final Runnable pre, final Function<T, Void> post);

    protected int getStatus(javax.transaction.Transaction tx) {
        try {
            return tx.getStatus();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isNotActive(javax.transaction.Transaction tx) {
        final int status = getStatus(tx);
        return (status != Status.STATUS_ACTIVE); // TODO -- more fine-grained?
    }

    protected boolean isActive(javax.transaction.Transaction tx) {
        return (getStatus(tx) == Status.STATUS_ACTIVE);
    }

    protected void handlePost(javax.transaction.Transaction tx, final Runnable post) {
        if (tx == null || isNotActive(tx)) {
            post.run();
        } else if (isActive(tx)) {
            try {
                tx.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                    }

                    public void afterCompletion(int status) {
                        if (status == Status.STATUS_COMMITTED) {
                            post.run();
                        }
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <T> Future<T> handleGetWithPost(final Future<T> wrap, final javax.transaction.Transaction tx, final Runnable post) {
        return new PostFuture<T>(wrap) {
            protected void after(T result) {
                handlePost(tx, post);
            }
        };
    }

    protected Future<Entity> doGet(final Transaction transaction, final Key key) {
        final Map<Key, Entity> map = new LinkedHashMap<Key, Entity>();
        final Runnable pre = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePreGetCallbacks(AbstractDatastoreService.this, Lists.newArrayList(key), map);
            }
        };
        final Function<Entity, Void> post = new Function<Entity, Void>() {
            public Void apply(Entity input) {
                if (input != null) {
                    getDatastoreCallbacks().executePostLoadCallbacks(postTxProvider(transaction), input);
                }
                return null;
            }
        };

        return wrap(transaction, new Callable<Entity>() {
            public Entity call() throws Exception {
                final Entity previous = map.get(key);
                return (previous == null) ? getDelegate().get(transaction, key) : previous;
            }
        }, pre, post);
    }

    protected Future<Key> doPut(final Transaction transaction, final Entity entity, final boolean applyPost) {
        final Runnable pre = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePrePutCallbacks(AbstractDatastoreService.this, Lists.newArrayList(entity));
            }
        };
        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostPutCallbacks(postTxProvider(transaction), Lists.newArrayList(entity));
            }
        };
        final Future<Key> wrap = wrap(transaction, new Callable<Key>() {
            public Key call() throws Exception {
                return getDelegate().put(transaction, entity, applyPost ? post : null);
            }
        }, pre, null); // do not add post!

        if (applyPost) {
            return wrap;
        } else {
            return handleGetWithPost(wrap, JBossTransaction.getTx(), post);
        }
    }

    public PreparedQuery prepare(Query query) {
        getDatastoreCallbacks().executePreQueryCallbacks(this, query);
        return getDelegate().prepare(query);
    }

    public PreparedQuery prepare(Transaction transaction, Query query) {
        getDatastoreCallbacks().executePreQueryCallbacks(this, query);
        return getDelegate().prepare(transaction, query);
    }

    public Transaction getCurrentTransaction() {
        return getDelegate().getCurrentTransaction();
    }

    public Transaction getCurrentTransaction(Transaction transaction) {
        return getDelegate().getCurrentTransaction(transaction);
    }

    public Collection<Transaction> getActiveTransactions() {
        return getDelegate().getActiveTransactions();
    }

    public DatastoreServiceConfig getDatastoreServiceConfig() {
        return getDelegate().getDatastoreServiceConfig();
    }

    public Iterator<Entity> getAllEntitiesIterator() {
        return getDelegate().getAllEntitiesIterator();
    }

    /**
     * Testing only!
     */
    public void clearCache() {
        getDelegate().clearCache();
    }
}
