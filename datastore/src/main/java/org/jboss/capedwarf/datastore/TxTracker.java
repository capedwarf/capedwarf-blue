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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.Transaction;

import com.google.appengine.api.datastore.Key;

/**
 * Track tx usage wrt entity groups.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class TxTracker {
    private static final ConcurrentMap<Key, Transaction> usedRoots = new ConcurrentHashMap<Key, Transaction>();

    // TODO -- handle this globally

    static void track(Key currentRoot) {
        final Transaction current = CapedwarfTransaction.getTx();
        final Transaction previous = usedRoots.putIfAbsent(currentRoot, current);
        if (previous != null && current.equals(previous) == false) {
            throw new ConcurrentModificationException("Different transactions on same entity group: " + currentRoot);
        }
    }

    static void remove(Key currentRoot) {
        usedRoots.remove(currentRoot);
    }

    static void dump() {
        System.err.println("Used roots: " + usedRoots);
    }
}
