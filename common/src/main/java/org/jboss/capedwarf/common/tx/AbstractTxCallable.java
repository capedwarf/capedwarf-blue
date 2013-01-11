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

package org.jboss.capedwarf.common.tx;

import java.util.concurrent.Callable;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.capedwarf.common.util.Util;

/**
 * Abstract tx callable.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractTxCallable<R> implements Callable<R> {
    private boolean newTx; // not thread safe

    public Callable<R> setNewTx(boolean newTx) {
        this.newTx = newTx;
        return this;
    }

    protected abstract R callInTx() throws Exception;

    protected abstract TransactionManager getTransactionManager();
    
    public R call() throws Exception {
        final TransactionManager tm = getTransactionManager();
        if (tm == null)
            throw new IllegalArgumentException("Null transaction manager!");

        final boolean useNewTx = newTx;
        boolean error = false;
        final Transaction previous = (useNewTx) ? tm.suspend() : tm.getTransaction();
        try {
            if (useNewTx || previous == null) {
                tm.begin();
            }
            try {
                return callInTx();
            } catch (Throwable t) {
                error = true;
                throw Util.toRuntimeException(t);
            } finally {
                if (useNewTx || previous == null) {
                    if (error || tm.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                        tm.rollback();
                    else
                        tm.commit();
                }
            }
        } finally {
            if (useNewTx && previous != null)
                tm.resume(previous);
        }
    }
}
