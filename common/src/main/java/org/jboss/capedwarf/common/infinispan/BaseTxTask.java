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

package org.jboss.capedwarf.common.infinispan;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.util.Set;

/**
 * Base Tx task.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class BaseTxTask<K, V, R> implements DistributedCallable<K, V, R>, Serializable {
    private transient Cache<K, V> cache;

    protected Cache<K, V> getCache() {
        return cache;
    }

    public void setEnvironment(Cache<K, V> cache, Set<K> inputKeys) {
        this.cache = cache;
    }

    protected abstract R callInTx() throws Exception;

    public R call() throws Exception {
        final AdvancedCache<K, V> ac = getCache().getAdvancedCache();
        final TransactionManager tm = ac.getTransactionManager();

        boolean error = false;
        final Transaction previous = tm.suspend();
        try {
            tm.begin();
            try {
                return callInTx();
            } catch (Throwable t) {
                error = true;
                throw (t instanceof RuntimeException) ? (RuntimeException) t : new RuntimeException(t);
            } finally {
                if (error || tm.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    tm.rollback();
                else
                    tm.commit();
            }
        } finally {
            if (previous != null)
                tm.resume(previous);
        }
    }
}
