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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import com.google.appengine.api.datastore.Entity;
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
    private final Set<Key> keys;

    private EntityGroupTracker(Transaction tx) {
        this.tx = tx;
        this.keys = new HashSet<Key>();
    }

    static void trackEntity(com.google.appengine.api.datastore.Transaction tx, Entity entity) throws Exception {
        final JBossTransaction jtx = (JBossTransaction) tx;
        final Transaction transaction = jtx.getTx();
        EntityGroupTracker egt = trackers.get(transaction);
        if (egt == null) {
            egt = new EntityGroupTracker(transaction);
            transaction.registerSynchronization(egt);
            trackers.put(transaction, egt);
        }
        egt.trackEntity(entity);
    }

    private void trackEntity(Entity entity) {
        final Key key = entity.getKey();
        if (key.getParent() == null)
            roots++;

        if (roots > 1)
            throw new IllegalArgumentException("can't operate on multiple entity groups in a single transaction.");

        keys.add(key);
    }

    private void check() {
        if (keys.size() < 2)
            return;

        int counter = 0;
        for (Key k : keys) {
            Key parent = k.getParent();
            if (parent == null || keys.contains(parent) == false) {
                counter++;
            }
            if (counter > 1)
                throw new IllegalArgumentException("Too many different entity groups!");
        }
    }

    public void beforeCompletion() {
        trackers.remove(tx);
        check();
    }

    public void afterCompletion(int status) {
    }
}
