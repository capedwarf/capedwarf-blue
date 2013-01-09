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
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transaction;

/**
 * Track tx tasks.
 * Cannot commit or rollback until they are finished.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class TxTasks {
    private static Map<Transaction, AtomicInteger> counters = new ConcurrentHashMap<Transaction, AtomicInteger>();

    static void begin(Transaction tx) {
        if (tx == null) return;

        AtomicInteger ai = counters.get(tx);
        if (ai == null) {
            ai = new AtomicInteger(0);
            counters.put(tx, ai);
        }
        ai.incrementAndGet();
    }

    static void end(Transaction tx) {
        if (tx == null) return;

        AtomicInteger ai = counters.get(tx);
        if (ai == null) {
            throw new IllegalStateException("No counter?!");
        }
        if (ai.decrementAndGet() == 0) {
            counters.remove(tx);
        }
    }

    static boolean finished(Transaction tx) {
        if (tx == null) return true;

        AtomicInteger ai = counters.get(tx);
        return (ai == null || ai.get() == 0);
    }

    static void clear(Transaction tx) {
        if (tx == null) return;

        counters.remove(tx);
    }
}
