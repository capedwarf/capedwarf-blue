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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

/**
 * Tx wrapper.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class TransactionWrapper implements javax.transaction.Transaction {
    private javax.transaction.Transaction delegate;
    private JBossTransaction transaction;

    TransactionWrapper(JBossTransaction transaction) {
        this.transaction = transaction;
        this.delegate = transaction.getTransaction();
    }

    JBossTransaction getTransaction() {
        return transaction;
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, SystemException {
        delegate.commit();
    }

    public void rollback() throws IllegalStateException, SystemException {
        delegate.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        return delegate.enlistResource(xaRes);
    }

    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        return delegate.delistResource(xaRes, flag);
    }

    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        delegate.registerSynchronization(sync);
    }
}
