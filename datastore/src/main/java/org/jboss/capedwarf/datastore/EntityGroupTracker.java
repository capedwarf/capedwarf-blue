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

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import com.google.appengine.api.datastore.Key;

/**
 * Track entity groups.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class EntityGroupTracker implements Synchronization {

    private static Map<Transaction, EntityGroupTracker> trackers = new ConcurrentHashMap<Transaction, EntityGroupTracker>();

    // TODO -- remove this
    private static final String LOG_REQUEST_ENTITY_KIND = "__org.jboss.capedwarf.LogRequest__";
    private static final String LOG_LINE_ENTITY_KIND = "__org.jboss.capedwarf.LogLine__";

    private final Transaction tx;
    private Key root;
    private boolean invalid;
    private final Map<Key, Key> keys;

    private EntityGroupTracker(Transaction tx) {
        this.tx = tx;
        this.keys = new ConcurrentHashMap<Key, Key>();
    }

    static EntityGroupTracker registerKey(Key key) throws Exception {
        final Transaction transaction = JBossTransaction.getTx();
        if (transaction == null)
            return null; // do not track w/o Tx

        final String kind = key.getKind();
        if (LOG_LINE_ENTITY_KIND.equals(kind) || LOG_REQUEST_ENTITY_KIND.equals(kind))
            return null; // TODO - hack, do not count logs

        EntityGroupTracker egt = trackers.get(transaction);
        if (egt == null) {
            egt = new EntityGroupTracker(transaction);
            transaction.registerSynchronization(egt);
            trackers.put(transaction, egt);
        }
        egt.addKey(key);
        return egt;
    }

    static void trackKey(Key key) throws Exception {
        final EntityGroupTracker egt = registerKey(key);
        if (egt != null) {
            egt.checkRoot();
        }
    }

    static void check() {
        final Transaction transaction = JBossTransaction.getTx();
        if (transaction == null)
            return; // nothing to check

        EntityGroupTracker egt = trackers.get(transaction);
        if (egt != null) {
            egt.checkRoot();
        }
    }

    private Key addKey(Key key) {
        Key currentRoot = getRoot(key);
        if (root == null) {
            root = currentRoot;
        } else if (root.equals(currentRoot) == false){
            invalid = true;
        }
        keys.put(key, currentRoot);
        return currentRoot;
    }

    private void checkRoot() {
        if (invalid) {
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
    }
}
