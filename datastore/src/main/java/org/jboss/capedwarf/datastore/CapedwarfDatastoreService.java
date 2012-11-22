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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.threads.DirectFuture;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfDatastoreService extends AbstractDatastoreService implements DatastoreService {
    public CapedwarfDatastoreService() {
        this(null);
    }

    public CapedwarfDatastoreService(DatastoreServiceConfig config) {
        super(new DatastoreServiceImpl(config));
    }

    protected CurrentTransactionProvider postTxProvider(Transaction tx) {
        return this;
    }

    protected <T> Future<T> wrap(final Transaction transaction, final Callable<T> callable, final Runnable pre, final Function<T, Void> post) {
        return DirectFuture.create(new Callable<T>() {
            public T call() throws Exception {
                if (pre != null) {
                    pre.run();
                }
                final T result = callable.call();
                if (post != null) {
                    post.apply(result);
                }
                return result;
            }
        });
    }

    protected <T> T unwrap(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Entity get(Key key) throws EntityNotFoundException {
        return get(getCurrentTransaction(null), key);
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        final Entity result = unwrap(doGet(transaction, key));
        if (result == null) {
            throw new EntityNotFoundException(key);
        }
        return result;
    }

    public Map<Key, Entity> get(Iterable<Key> keys) {
        return get(getCurrentTransaction(null), keys);
    }

    public Map<Key, Entity> get(final Transaction transaction, final Iterable<Key> keys) {
        final Map<Key, Entity> map = new LinkedHashMap<Key, Entity>();

        getDatastoreCallbacks().executePreGetCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(keys), map);

        final List<Key> requiredKeys = Lists.newArrayList(keys);
        if (map.isEmpty() == false) {
            requiredKeys.removeAll(map.keySet()); // remove manually added keys
        }

        for (Key key : requiredKeys) {
            final Entity entity = getDelegate().get(transaction, key);
            if (entity != null) {
                map.put(key, entity);
            }
        }

        getDatastoreCallbacks().executePostLoadCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(map.values()));

        return map;
    }

    public Key put(Entity entity) {
        return put(getCurrentTransaction(null), entity);
    }

    public Key put(Transaction transaction, Entity entity) {
        return unwrap(doPut(transaction, entity, true));
    }

    public List<Key> put(Iterable<Entity> entities) {
        return put(getCurrentTransaction(null), entities);
    }

    public List<Key> put(Transaction transaction, final Iterable<Entity> entities) {
        getDatastoreCallbacks().executePrePutCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(entities));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostPutCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(entities));
            }
        };

        return getDelegate().put(transaction, entities, post);
    }

    public void delete(Key... keys) {
        delete(getCurrentTransaction(null), keys);
    }

    public void delete(Transaction transaction, Key... keys) {
        delete(transaction, Arrays.asList(keys));
    }

    public void delete(Iterable<Key> keys) {
        delete(getCurrentTransaction(null), keys);
    }

    public void delete(final Transaction transaction, final Iterable<Key> keys) {
        getDatastoreCallbacks().executePreDeleteCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(keys));

        final Runnable post = new Runnable() {
            public void run() {
                getDatastoreCallbacks().executePostDeleteCallbacks(CapedwarfDatastoreService.this, Lists.newArrayList(keys));
            }
        };
        getDelegate().delete(transaction, keys, post);
    }

    public Transaction beginTransaction() {
        return beginTransaction(TransactionOptions.Builder.withDefaults());
    }

    public Transaction beginTransaction(final TransactionOptions transactionOptions) {
        return getDelegate().beginTransaction(transactionOptions);
    }

    public KeyRange allocateIds(String kind, long num) {
        return allocateIds(null, kind, num);
    }

    public KeyRange allocateIds(Key key, String s, long l) {
        return getDelegate().allocateIds(key, s, l);
    }

    public KeyRangeState allocateIdRange(KeyRange keys) {
        return getDelegate().allocateIdRange(keys);
    }

    public DatastoreAttributes getDatastoreAttributes() {
        return getDelegate().getDatastoreAttributes();
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return getDelegate().getIndexes();
    }
}
