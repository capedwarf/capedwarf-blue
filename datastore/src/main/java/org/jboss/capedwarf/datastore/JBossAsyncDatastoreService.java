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

    DatastoreServiceImpl getDelegate() {
        return (DatastoreServiceImpl) super.getDelegate();
    }

    protected <T> Future<T> wrap(final Callable<T> callable) {
        return ExecutorFactory.wrap(callable);
    }

    public Future<Transaction> beginTransaction() {
        return wrap(new Callable<Transaction>() {
            public Transaction call() throws Exception {
                return getDelegate().beginTransaction();
            }
        });
    }

    public Future<Transaction> beginTransaction(final TransactionOptions transactionOptions) {
        return wrap(new Callable<Transaction>() {
            public Transaction call() throws Exception {
                return getDelegate().beginTransaction(transactionOptions);
            }
        });
    }

    public Future<Entity> get(final Key key) {
        return wrap(new Callable<Entity>() {
            public Entity call() throws Exception {
                return getDelegate().get(key);
            }
        });
    }

    public Future<Entity> get(final Transaction transaction, final Key key) {
        return wrap(new Callable<Entity>() {
            public Entity call() throws Exception {
                return getDelegate().get(transaction, key);
            }
        });
    }

    public Future<Map<Key, Entity>> get(final Iterable<Key> keyIterable) {
        return wrap(new Callable<Map<Key, Entity>>() {
            public Map<Key, Entity> call() throws Exception {
                return getDelegate().get(keyIterable);
            }
        });
    }

    public Future<Map<Key, Entity>> get(final Transaction transaction, final Iterable<Key> keyIterable) {
        return wrap(new Callable<Map<Key, Entity>>() {
            public Map<Key, Entity> call() throws Exception {
                return getDelegate().get(transaction, keyIterable);
            }
        });
    }

    public Future<Key> put(final Entity entity) {
        return wrap(new Callable<Key>() {
            public Key call() throws Exception {
                return getDelegate().put(entity);
            }
        });
    }

    public Future<Key> put(final Transaction transaction, final Entity entity) {
        return wrap(new Callable<Key>() {
            public Key call() throws Exception {
                return getDelegate().put(transaction, entity);
            }
        });
    }

    public Future<List<Key>> put(final Iterable<Entity> entityIterable) {
        return wrap(new Callable<List<Key>>() {
            public List<Key> call() throws Exception {
                return getDelegate().put(entityIterable);
            }
        });
    }

    public Future<List<Key>> put(final Transaction transaction, final Iterable<Entity> entityIterable) {
        return wrap(new Callable<List<Key>>() {
            public List<Key> call() throws Exception {
                return getDelegate().put(transaction, entityIterable);
            }
        });
    }

    public Future<Void> delete(final Key... keys) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                getDelegate().delete(keys);
                return null;
            }
        });
    }

    public Future<Void> delete(final Transaction transaction, final Key... keys) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                getDelegate().delete(transaction, keys);
                return null;
            }
        });
    }

    public Future<Void> delete(final Iterable<Key> keyIterable) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                getDelegate().delete(keyIterable);
                return null;
            }
        });
    }

    public Future<Void> delete(final Transaction transaction, final Iterable<Key> keyIterable) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                getDelegate().delete(transaction, keyIterable);
                return null;
            }
        });
    }

    public Future<KeyRange> allocateIds(final String s, final long l) {
        return wrap(new Callable<KeyRange>() {
            public KeyRange call() throws Exception {
                return getDelegate().allocateIds(s, l);
            }
        });
    }

    public Future<KeyRange> allocateIds(final Key key, final String s, final long l) {
        return wrap(new Callable<KeyRange>() {
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
