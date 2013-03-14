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
import java.util.Iterator;
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
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.compatibility.Compatibility;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.MapKey;
import org.jboss.capedwarf.shared.components.Slot;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class DatastoreServiceImpl extends BaseDatastoreServiceImpl implements DatastoreServiceInternal {
    private static final MethodInvocation<Void> setId = ReflectionUtils.cacheMethod(Key.class, "setId", Long.TYPE);
    private static final MethodInvocation<Void> setChecked = ReflectionUtils.cacheMethod(Key.class, "setChecked", Boolean.TYPE);
    private final static TargetInvocation<Boolean> isChecked = ReflectionUtils.cacheInvocation(Key.class, "isChecked");

    private DatastoreAttributes datastoreAttributes;
    private volatile Map<String, Integer> allocationsMap;

    private final boolean async; // do we use async frontend
    private final EntityModifier entityModifier;

    public DatastoreServiceImpl() {
        this(null);
    }

    public DatastoreServiceImpl(DatastoreServiceConfig config) {
        this(config, false);
    }

    private DatastoreServiceImpl(DatastoreServiceConfig config, boolean async) {
        super(config);
        Compatibility instance = Compatibility.getInstance();
        boolean enabled = instance.isEnabled(Compatibility.Feature.IGNORE_ENTITY_PROPERTY_CONVERSION);
        entityModifier = enabled ? CloningEntityModifier.INSTANCE : EntityModifierImpl.INSTANCE;
        this.async = async;
    }

    public static DatastoreServiceInternal async(DatastoreServiceConfig config) {
        return new DatastoreServiceImpl(config, true);
    }

    static void applyKeyChecked(Key original, Key clone) {
        setChecked.invoke(original, new Object[]{isChecked.invokeUnchecked(clone)});
    }

    static void applyKeyChecked(Entity original, Entity clone) {
        applyKeyChecked(original.getKey(), clone.getKey());
    }

    protected Map<String, Integer> getAllocationsMap() {
        if (allocationsMap == null) {
            synchronized (this) {
                if (allocationsMap == null) {
                    MapKey<String, Integer> key = new MapKey<String, Integer>(EnvAppIdFactory.INSTANCE, Slot.ALLOCATIONS_MAP);
                    allocationsMap = ComponentRegistry.getInstance().getComponent(key);
                }
            }
        }
        return allocationsMap;
    }

    protected AllocationTuple getRangeStart(Key parent, String kind, long num) {
        final SequenceTuple st = SequenceTuple.getSequenceTuple(getAllocationsMap(), kind);
        long asNum = st.getAllocationSize() * num;
        long start = KeyGenerator.generateRange(appId, parent, st.getSequenceName(), asNum);
        return new AllocationTuple(start, asNum);
    }

    public Entity get(Transaction tx, Key key) {
        final javax.transaction.Transaction transaction = beforeTx(tx);
        try {
            EntityGroupTracker.trackKey(key);
            Entity entity = store.get(key);
            return makeDefensiveCopy(entity);
        } finally {
            afterTx(transaction);
        }
    }

    private Entity makeDefensiveCopy(Entity entity) {
        return entity == null ? null : entity.clone();
    }

    public Key put(Transaction tx, Entity entity, Runnable post) {
        return put(tx, Collections.singletonList(entity), post).get(0);
    }

    public List<Key> put(Transaction tx, Iterable<Entity> entities, Runnable post) {
        javax.transaction.Transaction current = null;
        if (async) {
            current = CapedwarfTransaction.getTx();
            TxTasks.begin(current);
        }
        try {
            javax.transaction.Transaction transaction = beforeTx(tx);
            try {
                List<Tuple> keyToEntityMap = new ArrayList<Tuple>();
                for (Entity entity : entities) {
                    assignIdIfNeeded(entity);
                    Key key = entity.getKey();
                    EntityGroupTracker.trackKey(key);
                    keyToEntityMap.add(new Tuple(key, entityModifier.modify(entity)));
                }
                putInTx(keyToEntityMap, post);
                return Lists.transform(keyToEntityMap, FN);
            } finally {
                afterTx(transaction);
            }
        } finally {
            if (async) {
                TxTasks.end(current);
            }
        }
    }

    private void assignIdIfNeeded(Entity entity) {
        Key key = entity.getKey();
        if (key.isComplete() == false) {
            Long id = getRangeStart(key.getParent(), key.getKind(), 1).getStart();
            setId.invoke(key, new Object[]{id});
        } else if (isChecked.invokeUnchecked(key) == false) {
            SequenceTuple st = SequenceTuple.getSequenceTuple(getAllocationsMap(), key.getKind());
            String sequenceName = st.getSequenceName();
            long allocationSize = st.getAllocationSize();
            KeyGenerator.updateRange(appId, key.getId(), sequenceName, allocationSize);
        }
        setChecked.invoke(key, new Object[]{true});
    }

    @Override
    public void delete(Transaction tx, Iterable<Key> keys, Runnable post) {
        javax.transaction.Transaction current = null;
        if (async) {
            current = CapedwarfTransaction.getTx();
            TxTasks.begin(current);
        }
        try {
            final javax.transaction.Transaction transaction = beforeTx(tx);
            try {
                for (Key key : keys) {
                    EntityGroupTracker.trackKey(key);
                }
                removeInTx(keys, post);
            } finally {
                afterTx(transaction);
            }
        } finally {
            if (async) {
                TxTasks.end(current);
            }
        }
    }

    public Transaction beginTransaction(TransactionOptions options) {
        return CapedwarfTransaction.newTransaction(options);
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
        final SequenceTuple st = SequenceTuple.getSequenceTuple(getAllocationsMap(), kind);
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
    protected void putInTx(final List<Tuple> keyToEntityMap, final Runnable post) {
        final javax.transaction.Transaction tx = CapedwarfTransaction.getTx();
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
        final javax.transaction.Transaction tx = CapedwarfTransaction.getTx();
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

    private void doPut(List<Tuple> keyToEntityMap, Runnable post) {
        for (Tuple tuple : keyToEntityMap) {
            ignoreReturnStore.put(tuple.key, tuple.entity);
        }
        if (post != null) {
            post.run();
        }
    }

    private void doRemove(Iterable<Key> keys, Runnable post) {
        for (Key key : keys) {
            ignoreReturnStore.remove(key);
        }
        if (post != null) {
            post.run();
        }
    }

    public Iterator<Entity> getAllEntitiesIterator() {
        return store.values().iterator();   // TODO: temp impl, which only returns entities on current node
    }

    /**
     * Testing only!
     */
    public void clearCache() {
        store.clear();
    }

    private static class Tuple {
        Key key;
        Entity entity;

        private Tuple(Key key, Entity entity) {
            this.key = key;
            this.entity = entity;
        }
    }

    private static final Tuple2Key FN = new Tuple2Key();

    private static class Tuple2Key implements Function<Tuple, Key> {
        public Key apply(Tuple input) {
            return input.key;
        }
    }
}
