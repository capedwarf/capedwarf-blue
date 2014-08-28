/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentMap;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.config.CacheName;

/**
 * Cluster wide tracking.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ClusteredTxTracker implements TxTracker {
    private volatile ConcurrentMap<String, String> usedRoots;

    private ConcurrentMap<String, String> getUsedRoots() {
        if (usedRoots == null) {
            usedRoots = InfinispanUtils.getCache(AppIdFactory.getAppId(), CacheName.DIST);
        }
        return usedRoots;
    }

    private static String mask(Key key) {
        return "TxT:" + KeyFactory.keyToString(key);
    }

    public void track(Key currentRoot) {
        final Transaction current = CapedwarfTransaction.currentTransaction();

        final String currentId = current.getId();
        final String previousId = getUsedRoots().putIfAbsent(mask(currentRoot), currentId);

        if (previousId != null && previousId.equals(currentId) == false) {
            throw new ConcurrentModificationException("Different transactions on same entity group: " + currentRoot);
        }
    }

    public void beforeCompletion(Key currentRoot) {
    }

    public void afterCompletion(int status, Key currentRoot) {
        final javax.transaction.Transaction tx = CapedwarfTransaction.suspendTx();
        try {
            getUsedRoots().remove(mask(currentRoot));
        } finally {
            CapedwarfTransaction.resumeTx(tx);
        }
    }

    public void dump() {
        System.err.println("Used roots: " + getUsedRoots());
    }
}
