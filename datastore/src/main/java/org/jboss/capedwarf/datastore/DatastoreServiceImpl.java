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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Synchronization;

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
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class DatastoreServiceImpl extends BaseDatastoreServiceImpl implements DatastoreService {
    private DatastoreAttributes datastoreAttributes;
    private volatile Map<String, Integer> allocationsMap;

    private static final String SEQUENCE_POSTFIX = "_SEQUENCE__"; // GAE's SequenceGenerator impl detail

    private final EntityModifier entityModifier = EntityModifierImpl.INSTANCE;

    public DatastoreServiceImpl() {
    }

    public DatastoreServiceImpl(DatastoreServiceConfig config) {
        super(config);
    }

    protected Map<String, Integer> getAllocationsMap() {
        if (allocationsMap == null) {
            synchronized (this) {
                if (allocationsMap == null) {
                    //noinspection unchecked
                    allocationsMap = JndiLookupUtils.lookup(
                            "jndi.persistence.allocationsMap",
                            Map.class,
                            "java:jboss/capedwarf/persistence/allocationsMap/" + Application.getAppId()
                    );
                }
            }
        }
        return allocationsMap;
    }

    protected SequenceTuple getSequenceTuple(String kind) {
        final String key;
        final int p = kind.lastIndexOf(SEQUENCE_POSTFIX);
        if (p > 0) {
            key = kind.substring(0 , p);
        } else {
            key = kind;
        }
        // search w/o _SEQUENCE__, to find explicit ones
        Integer allocationSize = getAllocationsMap().get(key);
        final String sequenceName;
        if (allocationSize != null) {
            // impl detail, on how to diff default vs. explicit seq names
            if (allocationSize > 0) {
                sequenceName = key + SEQUENCE_POSTFIX; // by default add _SEQUENCE__
            } else {
                allocationSize = (-1) * allocationSize;
                sequenceName = key; // use explicit sequence name
            }
        } else {
            allocationSize = 1;
            sequenceName = key + SEQUENCE_POSTFIX; // by default add _SEQUENCE__
        }
        return new SequenceTuple(sequenceName, allocationSize);
    }

    protected AllocationTuple getRangeStart(Key parent, String kind, long num) {
        final SequenceTuple st = getSequenceTuple(kind);
        long asNum = st.getAllocationSize() * num;
        long start = KeyGenerator.generateRange(parent, st.getSequenceName(), asNum);
        return new AllocationTuple(start, asNum);
    }

    public Entity get(Key key) throws EntityNotFoundException {
        return get(getCurrentTransaction(null), key);
    }

    public Entity get(Transaction tx, Key key) throws EntityNotFoundException {
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            trackKey(key);
            Entity entity = store.get(key);
            if (entity == null)
                throw new EntityNotFoundException(key);
            else
                return entity.clone();
        } finally {
            afterTx(transaction);
        }
    }

    public Map<Key, Entity> get(Iterable<Key> keyIterable) {
        return get(getCurrentTransaction(null), keyIterable);
    }

    public Map<Key, Entity> get(Transaction tx, Iterable<Key> keyIterable) {
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            Map<Key, Entity> result = new HashMap<Key, Entity>();
            for (Key key : keyIterable) {
                trackKey(key);
                Entity entity = store.get(key);
                if (entity != null) {
                    result.put(key, entity.clone());
                }
            }
            return result;
        } finally {
            afterTx(transaction);
        }
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

    public List<Key> put(Transaction tx, Iterable<Entity> entities) {
        javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            List<Key> list = new ArrayList<Key>();
            for (Entity entity : entities) {
                Key key = entity.getKey();
                if (key.isComplete() == false) {
                    long id = getRangeStart(key.getParent(), key.getKind(), 1).getStart();
                    ReflectionUtils.invokeInstanceMethod(key, "setId", Long.TYPE, id);
                }
                trackKey(key);
                putInTx(key, entityModifier.modify(entity));
                list.add(key);
            }
            return list;
        } finally {
            afterTx(transaction);
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
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            for (Key key : keyIterable) {
                trackKey(key);
                removeInTx(key);
            }
        } finally {
            afterTx(transaction);
        }
    }

    public Transaction beginTransaction() {
        return beginTransaction(TransactionOptions.Builder.withDefaults());
    }

    public Transaction beginTransaction(TransactionOptions options) {
        return JBossTransaction.newTransaction(options);
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return null;  // TODO
    }

    public KeyRange allocateIds(String kind, long num) {
        return allocateIds(null, kind, num);
    }

    public KeyRange allocateIds(Key parent, String kind, long num) {
        final AllocationTuple at = getRangeStart(parent, kind, num);
        final long start = at.getStart();
        return new KeyRange(parent, kind, start, start + at.getNum() - 1);
    }

    public KeyRangeState allocateIdRange(KeyRange keyRange) {
        final String kind = keyRange.getStart().getKind();
        final SequenceTuple st = getSequenceTuple(kind);
        return KeyGenerator.checkRange(keyRange, st.getSequenceName());
    }

    public DatastoreAttributes getDatastoreAttributes() {
        if (datastoreAttributes == null)
            datastoreAttributes = ReflectionUtils.newInstance(DatastoreAttributes.class);
        return datastoreAttributes;
    }

    /**
     * Delay put if the tx is active.
     *
     * @param key the key
     * @param entity the entity
     */
    protected void putInTx(final Key key, final Entity entity) {
        javax.transaction.Transaction tx = JBossTransaction.getTx();
        if (tx == null) {
            store.put(key, entity);
        } else {
            try {
                tx.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                        store.put(key, entity);
                    }

                    public void afterCompletion(int status) {
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Delay remove if the tx is active.
     *
     * @param key the key
     */
    protected void removeInTx(final Key key) {
        javax.transaction.Transaction tx = JBossTransaction.getTx();
        if (tx == null) {
            store.remove(key);
        } else {
            try {
                tx.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                        store.remove(key);
                    }

                    public void afterCompletion(int status) {
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Testing only!
     */
    public void clearCache() {
        store.clear();
    }

    /**
     * Register key.
     *
     * @param key the key to track
     */
    static void registerKey(Key key) {
        try {
            if (key != null) {
                EntityGroupTracker.registerKey(key);
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Track key.
     *
     * @param key the key to track
     */
    static void trackKey(Key key) {
        try {
            if (key != null) {
                EntityGroupTracker.trackKey(key);
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Check keys.
     */
    static void checkKeys() {
        EntityGroupTracker.check();
    }
}
