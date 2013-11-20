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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import com.google.appengine.repackaged.com.google.common.collect.Iterators;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.MapKey;
import org.jboss.capedwarf.shared.components.Slot;
import org.jboss.capedwarf.shared.config.IndexesXml;

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

    private Map<Index, Index.IndexState> indexes;

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
        Compatibility instance = CompatibilityUtils.getInstance();
        boolean enabled = instance.isEnabled(Compatibility.Feature.IGNORE_ENTITY_PROPERTY_CONVERSION);
        entityModifier = enabled ? CloningEntityModifier.INSTANCE : EntityModifierImpl.INSTANCE;
        this.async = async;
    }

    public static DatastoreServiceInternal async(DatastoreServiceConfig config) {
        if (config.getImplicitTransactionManagementPolicy() == ImplicitTransactionManagementPolicy.AUTO) {
            throw new IllegalArgumentException("Async Datastore service does not support ImplicitTransactionManagementPolicy.AUTO!");
        }
        return new DatastoreServiceImpl(config, true);
    }

    static void applyKeyChecked(Key original, Key clone) {
        setChecked.invokeWithTarget(original, isChecked.invokeUnchecked(clone));
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

    protected Entity getEntity(Key key) {
        if (Entities.ENTITY_GROUP_METADATA_KIND.equals(key.getKind())) {
            return getEntityGroupMetadataEntity(key);
        }

        EntityGroupTracker.trackKey(key);
        Entity entity = store.get(key);
        return EntityUtils.cloneEntity(entity);
    }

    public Entity get(Transaction tx, Key key) {
        try {
            final javax.transaction.Transaction transaction = beforeTx(tx);
            try {
                return getEntity(key);
            } finally {
                afterTx(transaction);
            }
        } finally {
            if (async) {
                TxTasks.end();
            }
        }
    }

    public void get(Transaction tx, List<Key> keys, Map<Key, Entity> map) {
        try {
            final javax.transaction.Transaction transaction = beforeTx(tx);
            try {
                for (Key key : keys) {
                    Entity entity = getEntity(key);
                    if (entity != null) {
                        map.put(key, entity);
                    }
                }
            } finally {
                afterTx(transaction);
            }
        } finally {
            if (async) {
                TxTasks.end();
            }
        }
    }

    public Key put(Transaction tx, Entity entity, Runnable post) {
        return put(tx, Collections.singletonList(entity), post).get(0);
    }

    public List<Key> put(Transaction tx, Iterable<Entity> entities, Runnable post) {
        try {
            javax.transaction.Transaction transaction = beforeTx(tx);
            try {
                List<Tuple> keyToEntityMap = new ArrayList<Tuple>();
                for (Entity entity : entities) {
                    checkEntity(entity);
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
                TxTasks.end();
            }
        }
    }

    private void checkEntity(Entity entity) {
        final String kind = entity.getKind();
        if (KindUtils.inProgress(KindUtils.Type.METADATA) == false && KindUtils.match(kind, KindUtils.Type.METADATA)) {
            throw new IllegalArgumentException("Cannot store metadata kind: " + kind);
        }
    }

    private void assignIdIfNeeded(Entity entity) {
        Key key = entity.getKey();
        if (key.isComplete() == false) {
            Long id = getRangeStart(key.getParent(), key.getKind(), 1).getStart();
            setId.invokeWithTarget(key, id);
        } else if (isChecked.invokeUnchecked(key) == false) {
            SequenceTuple st = SequenceTuple.getSequenceTuple(getAllocationsMap(), key.getKind());
            String sequenceName = st.getSequenceName();
            long allocationSize = st.getAllocationSize();
            KeyGenerator.updateRange(appId, key.getId(), sequenceName, allocationSize);
        }
        setChecked.invokeWithTarget(key, true);
    }

    @Override
    public void delete(Transaction tx, Iterable<Key> keys, Runnable post) {
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
                TxTasks.end();
            }
        }
    }

    public Transaction beginTransaction(TransactionOptions options) {
        return CapedwarfTransaction.newTransaction(options);
    }

    public synchronized Map<Index, Index.IndexState> getIndexes() {
        if (indexes == null) {
            indexes = new HashMap<Index, Index.IndexState>();
            long id = 0;
            IndexesXml indexesXml = CapedwarfEnvironment.getThreadLocalInstance().getIndexes();
            for (IndexesXml.Index i : indexesXml.getIndexes().values()) {
                List<Index.Property> properties = new ArrayList<Index.Property>();
                for (IndexesXml.Property p : i.getProperties()) {
                    Index.Property property = ReflectionUtils.newInstance(
                            Index.Property.class,
                            new Class[]{String.class, Query.SortDirection.class},
                            new Object[]{p.getName(), toDirection(p.getDirection())}
                    );
                    properties.add(property);
                }
                Index index = ReflectionUtils.newInstance(
                        Index.class,
                        new Class[]{Long.TYPE, String.class, Boolean.TYPE, List.class},
                        new Object[]{++id, i.getKind(), i.isAncestor(), properties}
                );
                indexes.put(index, Index.IndexState.SERVING);
            }
        }
        return indexes;
    }

    private static Query.SortDirection toDirection(String direction) {
        if ("ASC".equalsIgnoreCase(direction)) {
            return Query.SortDirection.ASCENDING;
        } else if ("DESC".equalsIgnoreCase(direction)) {
            return Query.SortDirection.DESCENDING;
        } else {
            throw new IllegalArgumentException("No such direction: " + direction);
        }
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
            putEntityGroupKey(tuple.key);
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
        return Iterators.filter(store.values().iterator(), new SkipMetadataAndStatsEntities());
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

    private static class SkipMetadataAndStatsEntities implements Predicate<Entity> {
        @Override
        public boolean apply(Entity entity) {
            return !KindUtils.match(entity.getKind(), KindUtils.Type.METADATA, KindUtils.Type.STATS);
        }
    }
}
