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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class DatastoreServiceImpl extends BaseDatastoreServiceImpl implements DatastoreServiceInternal {
    private static final MethodInvocation<Void> setId = ReflectionUtils.cacheMethod(Key.class, "setId", Long.TYPE);

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
                            "java:jboss/capedwarf/persistence/allocationsMap/" + appId
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
        long start = KeyGenerator.generateRange(appId, parent, st.getSequenceName(), asNum);
        return new AllocationTuple(start, asNum);
    }

    public Entity get(Transaction tx, Key key) {
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            EntityGroupTracker.trackKey(key);
            Entity entity = store.get(key);
            if (entity == null)
                return null;
            else
                return entity.clone();
        } finally {
            afterTx(transaction);
        }
    }

    public Key put(Transaction tx, Entity entity, Runnable post) {
        return put(tx, Collections.singletonList(entity), post).get(0);
    }

    public List<Key> put(Transaction tx, Iterable<Entity> entities, Runnable post) {
        javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            List<Key> keys = new ArrayList<Key>();
            Map<Key, Entity> keyToEntityMap = new HashMap<Key, Entity>();
            for (Entity entity : entities) {
                assignIdIfNeeded(entity);
                Key key = entity.getKey();
                EntityGroupTracker.trackKey(key);
                keys.add(key);
                keyToEntityMap.put(key, entityModifier.modify(entity));
            }
            putInTx(keyToEntityMap, post);
            return keys;
        } finally {
            afterTx(transaction);
        }
    }

    private void assignIdIfNeeded(Entity entity) {
        Key key = entity.getKey();
        if (key.isComplete() == false) {
            Long id = getRangeStart(key.getParent(), key.getKind(), 1).getStart();
            setId.invoke(key, new Object[]{id});
        }
    }

    @Override
    public void delete(Transaction tx, Iterable<Key> keys, Runnable post) {
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            for (Key key : keys) {
                EntityGroupTracker.trackKey(key);
            }
            removeInTx(keys, post);
        } finally {
            afterTx(transaction);
        }
    }

    public Transaction beginTransaction(TransactionOptions options) {
        return JBossTransaction.newTransaction(options);
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return null;  // TODO
    }

    public KeyRange allocateIds(Key parent, String kind, long num) {
        final AllocationTuple at = getRangeStart(parent, kind, num);
        final long start = at.getStart();
        return new KeyRange(parent, kind, start, start + at.getNum() - 1);
    }

    public DatastoreService.KeyRangeState allocateIdRange(KeyRange keyRange) {
        final String kind = keyRange.getStart().getKind();
        final SequenceTuple st = getSequenceTuple(kind);
        return KeyGenerator.checkRange(appId, keyRange, st.getSequenceName());
    }

    public synchronized DatastoreAttributes getDatastoreAttributes() {
        if (datastoreAttributes == null)
            datastoreAttributes = ReflectionUtils.newInstance(DatastoreAttributes.class);
        return datastoreAttributes;
    }

    /**
     * Delay put if the tx is active.
     *
     * @param post the post fn
     */
    protected void putInTx(final Map<Key, Entity> keyToEntityMap, final Runnable post) {
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        if (tx == null) {
            doPut(keyToEntityMap, post);
        } else {
            try {
                tx.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                        doPut(keyToEntityMap, null);
                    }

                    public void afterCompletion(int status) {
                        if (post != null && status == Status.STATUS_COMMITTED) {
                            post.run();
                        }
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
     * @param keys the keys to remove
     * @param post the post fn
     */
    protected void removeInTx(final Iterable<Key> keys, final Runnable post) {
        final javax.transaction.Transaction tx = JBossTransaction.getTx();
        if (tx == null) {
            doRemove(keys, post);
        } else {
            try {
                tx.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                        doRemove(keys, null);
                    }

                    public void afterCompletion(int status) {
                        if (post != null && status == Status.STATUS_COMMITTED) {
                            post.run();
                        }
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doPut(Map<Key, Entity> keyToEntityMap, Runnable post) {
        store.putAll(keyToEntityMap);
        if (post != null) {
            post.run();
        }
    }

    private void doRemove(Iterable<Key> keys, Runnable post) {
        for (Key key : keys) {
            store.remove(key);
        }
        if (post != null) {
            post.run();
        }
    }

    /**
     * Testing only!
     */
    public void clearCache() {
        store.clear();
    }
}
