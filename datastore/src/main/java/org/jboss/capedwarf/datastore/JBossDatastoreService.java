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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossDatastoreService extends AbstractDatastoreService implements DatastoreService {
    public JBossDatastoreService() {
        this(null);
    }

    public JBossDatastoreService(DatastoreServiceConfig config) {
        super(new DatastoreServiceImpl(config));
    }

    protected <T> T execute(final Callable<T> callable, final Runnable pre, final Runnable post) {
        if (pre != null) {
            pre.run();
        }
        final T result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (post != null) {
            post.run();
        }
        return result;
    }

    DatastoreServiceInternal getDelegate() {
        return (DatastoreServiceInternal) super.getDelegate();
    }
    
    public Entity get(Key key) throws EntityNotFoundException {
        return get(getCurrentTransaction(null), key);
    }

    public Entity get(final Transaction transaction, final Key key) throws EntityNotFoundException {
        return execute(new Callable<Entity>() {
            public Entity call() throws Exception {
                return getDelegate().get(transaction, key);
            }
        }, null, null);
    }

    public Map<Key, Entity> get(Iterable<Key> keys) {
        return get(getCurrentTransaction(null), keys);
    }

    public Map<Key, Entity> get(final Transaction transaction, final Iterable<Key> keys) {
        final Map<Key, Entity> map = new HashMap<Key, Entity>();
        for (Key key : keys) {
            try {
                map.put(key, get(key));
            } catch (EntityNotFoundException ignore) {
            }
        }
        return map;
    }

    public Key put(Entity entity) {
        return put(getCurrentTransaction(null), entity);
    }

    public Key put(final Transaction transaction, final Entity entity) {
        return execute(new Callable<Key>() {
            public Key call() throws Exception {
                return getDelegate().put(transaction, entity);
            }
        }, null, null);
    }

    public List<Key> put(Iterable<Entity> entities) {
        return put(getCurrentTransaction(null), entities);
    }

    public List<Key> put(Transaction transaction, Iterable<Entity> entities) {
        final List<Key> keys = new ArrayList<Key>();
        for (Entity e : entities) {
            keys.add(put(transaction, e));
        }
        return keys;
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
        for (Key k : keys) {
            final Key key = k;
            execute(new Callable<Object>() {
                public Object call() throws Exception {
                    getDelegate().delete(transaction, key);
                    return null;
                }
            }, null, null);
        }
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

    /**
     * Testing only!
     */
    public void clearCache() {
        getDelegate().clearCache();
    }
}
