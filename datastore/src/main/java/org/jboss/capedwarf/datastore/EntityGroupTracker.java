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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final Transaction tx;
    private int roots;
    private List<Key> parents;
    private final Set<Key> keys;

    private EntityGroupTracker(Transaction tx) {
        this.tx = tx;
        this.keys = new HashSet<Key>();
    }

    static void trackKey(Key key) throws Exception {
        final Transaction transaction = JBossTransaction.getTx();
        if (transaction == null)
            return; // do not track w/o Tx

        EntityGroupTracker egt = trackers.get(transaction);
        if (egt == null) {
            egt = new EntityGroupTracker(transaction);
            transaction.registerSynchronization(egt);
            trackers.put(transaction, egt);
        }
        egt.checkKey(key);
    }

    private void checkKey(Key key) {
        boolean illegalKey = false;

        Key p = key.getParent();
        if (p == null) {
            roots++;
        } else {
            if (parents == null) {
                parents = parents(p);
            } else {
                final List<Key> keys = parents(p);
                int N = parents.size();
                int k = keys.size();
                for (int i = 0; i < Math.min(N, k); i++) {
                    if (parents.get(i).equals(keys.get(i)) == false) {
                        illegalKey = true;
                        break;
                    }
                }
                if (illegalKey == false && k > N) {
                    parents = keys; // save longer parents
                }
            }
        }

        keys.add(key);

        if (roots > 1 || illegalKey)
            throw new IllegalArgumentException("can't operate on multiple entity groups in a single transaction. found: " + keys);
    }

    private List<Key> parents(Key p) {
        final List<Key> keys = new ArrayList<Key>();
        while (p != null) {
            keys.add(0, p);
            p = p.getParent();
        }
        return keys;
    }

    public void beforeCompletion() {
        trackers.remove(tx);
    }

    public void afterCompletion(int status) {
    }
}
