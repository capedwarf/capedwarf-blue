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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import com.google.appengine.api.datastore.Key;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * Track entity groups.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class EntityGroupTracker implements Synchronization {

    private static Map<Transaction, EntityGroupTracker> trackers = new ConcurrentHashMap<Transaction, EntityGroupTracker>();

    private final Transaction tx;
    private final TxChecker checker;
    private final Map<Key, Key> keys;

    private EntityGroupTracker(Transaction tx) {
        this.tx = tx;
        this.checker = CapedwarfTransaction.isXG() ? new XGTxChecker() : new SingleTxChecker();
        this.keys = new ConcurrentHashMap<Key, Key>();
    }

    static EntityGroupTracker registerKey(Key key) {
        if (key == null || ignoreTracking())
            return null;

        final Transaction transaction = CapedwarfTransaction.getTx();
        if (ignoreTracking(transaction))
            return null; // do not track w/o Tx

        if (KindUtils.isSpecial(key.getKind()))
            return null;

        EntityGroupTracker egt = trackers.get(transaction);
        if (egt == null) {
            egt = new EntityGroupTracker(transaction);
            egt.registerSynchronization(transaction);
            trackers.put(transaction, egt);
        }
        egt.addKey(key);
        return egt;
    }

    static void trackKey(Key key) {
        final EntityGroupTracker egt = registerKey(key);
        if (egt != null) {
            egt.checkRoot();
        }
    }

    static void check() {
        if (ignoreTracking())
            return;

        final Transaction transaction = CapedwarfTransaction.getTx();
        if (ignoreTracking(transaction))
            return; // nothing to check

        EntityGroupTracker egt = trackers.get(transaction);
        if (egt != null) {
            egt.checkRoot();
        }
    }

    private static boolean ignoreTracking() {
        Compatibility instance = Compatibility.getInstance();
        return instance.isEnabled(Compatibility.Feature.DISABLE_ENTITY_GROUPS);
    }

    private static boolean ignoreTracking(Transaction tx) {
        return (tx == null);
    }

    private void registerSynchronization(Transaction tx) {
        try {
            tx.registerSynchronization(this);
        } catch (RollbackException e) {
            throw new RuntimeException(e);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private Key addKey(Key key) {
        Key currentRoot = getRoot(key);
        keys.put(key, currentRoot);
        checker.add(currentRoot, key);
        return currentRoot;
    }

    private void checkRoot() {
        if (checker.isInvalid()) {
            throw new IllegalArgumentException("can't operate on multiple entity groups in a single transaction. found: " + keys.keySet());
        }
    }

    private static Key getRoot(Key key) {
        Key currentRoot = key;
        while (currentRoot.getParent() != null) {
            currentRoot = currentRoot.getParent();
        }
        return currentRoot;
    }

    public void beforeCompletion() {
        trackers.remove(tx);
    }

    public void afterCompletion(int status) {
        checker.clear();
    }
}
