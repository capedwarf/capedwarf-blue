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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossDatastoreService extends AbstractDatastoreService implements DatastoreService {
    private DatastoreAttributes datastoreAttributes;

    public Entity get(Key key) throws EntityNotFoundException {
        Entity entity = store.get(key);
        if (entity == null)
            throw new EntityNotFoundException(key);
        else
            return entity;
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        return get(key);
    }

    public Map<Key, Entity> get(Iterable<Key> keyIterable) {
        Map<Key, Entity> result = new HashMap<Key, Entity>();
        for (Key key : keyIterable)
            result.put(key, store.get(key));
        return result;
    }

    public Map<Key, Entity> get(Transaction transaction, Iterable<Key> keyIterable) {
        return get(keyIterable);
    }

    public Key put(Entity entity) {
        return put(getCurrentTransaction(null), entity);
    }

    public Key put(Transaction transaction, Entity entity) {
        return put(transaction, Collections.singleton(entity)).get(0);
    }

    public List<Key> put(Iterable<Entity> entityIterable) {
        return put(getCurrentTransaction(null), entityIterable);
    }

    public List<Key> put(Transaction tx, Iterable<Entity> entityIterable) {
        boolean newTx = (tx == null);
        if (newTx)
            tx = beginTransaction();

        try {
            List<Key> list = new ArrayList<Key>();
            for (Entity entity : entityIterable) {
                Key key = entity.getKey();
                if (key.isComplete() == false) {
                    long id = KeyGenerator.generateKeyId(key);
                    ReflectionUtils.invokeInstanceMethod(key, "setId", Long.TYPE, id);
                }
                EntityGroupTracker.trackEntity(tx, entity);
                store.put(key, modify(entity));
                list.add(key);
            }

            if (newTx) {
                newTx = false;
                tx.commit();
            }

            return list;
        } catch (Throwable t) {
            if (newTx)
                tx.rollback();
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * GAE does some funky stuff to certain property types:
     *     - Integer, Short, Byte types are stored as Long
     *     - empty collections are stored as null
     *     - elements of collections that are of type Integer, Short or Byte are also stored as Long
     *
     * @param original the original entity
     * @return fixed clone
     */
    protected Entity modify(Entity original) {
        final Entity clone = original.clone();
        final Map<String, Object> properties = original.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            final String name = entry.getKey();
            final Object v = entry.getValue();
            boolean unindexed = original.isUnindexedProperty(name);
            if (v instanceof Integer || v instanceof Short || v instanceof Byte) {
                Number number = (Number) v;
                setProperty(clone, name, number.longValue(), unindexed);
            } else if (v instanceof Collection) {
                Collection<?> collection = (Collection<?>) v;
                if (collection.isEmpty()) {
                    clone.setProperty(name, null);
                } else {
                    if (collection instanceof Set) {
                        replaceCollection(clone, name, unindexed, collection, new HashSet());
                    } else if (collection instanceof List) {
                        replaceCollection(clone, name, unindexed, collection, new ArrayList(collection.size()));
                    }
                }
            } else if (unindexed) {
                clone.setUnindexedProperty(name, v);
            }
        }
        return clone;
    }

    private void setProperty(Entity clone, String name, Object convertedValue, boolean unindexed) {
        if (unindexed) {
            clone.setUnindexedProperty(name, convertedValue);
        } else {
            clone.setProperty(name, convertedValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceCollection(Entity entity, String propertyName, boolean unindexed, Collection collection, Collection convertedCollection) {
        for (Object o : collection) {
            convertedCollection.add(convert(o));
        }
        setProperty(entity, propertyName, convertedCollection, unindexed);
    }

    private Object convert(Object o) {
        if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
            Number number = (Number) o;
            return number.longValue();
        } else {
            return o;
        }
    }

    public void delete(Key... keys) {
        delete(getCurrentTransaction(null), keys);
    }

    public void delete(Transaction transaction, Key... keys) {
        delete(transaction, Arrays.asList(keys));
    }

    public void delete(Iterable<Key> keyIterable) {
        delete(getCurrentTransaction(null), keyIterable);
    }

    public void delete(Transaction tx, Iterable<Key> keyIterable) {
        boolean newTx = (tx == null);
        if (newTx)
            tx = beginTransaction();

        try {
            for (Key key : keyIterable)
                store.remove(key);

            if (newTx) {
                newTx = false;
                tx.commit();
            }
        } catch (Throwable t) {
            if (newTx)
                tx.rollback();
            throw new RuntimeException(t);
        }
    }

    public Transaction beginTransaction() {
        return beginTransaction(TransactionOptions.Builder.withDefaults());
    }

    public Transaction beginTransaction(TransactionOptions options) {
        return JBossTransaction.newTransaction();
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return null;  // TODO
    }

    public KeyRange allocateIds(String kind, long num) {
        return allocateIds(null, kind, num);
    }

    public KeyRange allocateIds(Key parent, String kind, long num) {
        return KeyGenerator.generateRange(parent, kind, num);
    }

    public KeyRangeState allocateIdRange(KeyRange keyRange) {
        return KeyGenerator.checkRange(keyRange);
    }

    public DatastoreAttributes getDatastoreAttributes() {
        if (datastoreAttributes == null)
            datastoreAttributes = ReflectionUtils.newInstance(DatastoreAttributes.class);
        return datastoreAttributes;
    }

    public void clearCache() {
        store.clear();
    }
}
