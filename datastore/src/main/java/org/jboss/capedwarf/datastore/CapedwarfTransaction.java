/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.InvalidTransactionException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.common.tx.TxUtils;
import org.jboss.capedwarf.environment.EnvironmentFactory;

/**
 * JBoss GAE transaction.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class CapedwarfTransaction implements Transaction {
    private static final Logger log = Logger.getLogger(CapedwarfTransaction.class.getName());

    private final static TransactionManager tm = TxUtils.getTransactionManager();
    private final static ThreadLocal<Stack<CapedwarfTransaction>> current = new ThreadLocal<Stack<CapedwarfTransaction>>();

    private final TransactionOptions options;
    private ThreadLocal<javax.transaction.Transaction> transactions = new ThreadLocal<javax.transaction.Transaction>();

    private CapedwarfTransaction(TransactionOptions options) {
        this.options = options;
    }

    static Transaction newTransaction(TransactionOptions options) {
        Stack<CapedwarfTransaction> stack = current.get();
        if (stack == null) {
            stack = new Stack<CapedwarfTransaction>();
            current.set(stack);
        } else {
            CapedwarfTransaction jt = stack.peek();
            jt.suspend(); // suspend existing
        }
        try {
            tm.begin(); // being new
        } catch (Exception e) {
            if (stack.isEmpty())
                current.remove();
            else
                stack.peek().resume(true); // resume back previous

            throw new DatastoreFailureException("Cannot begin tx.", e);
        }
        final CapedwarfTransaction tx = new CapedwarfTransaction(options);
        stack.push(tx);
        return tx;
    }

    javax.transaction.Transaction getTransaction() {
        return transactions.get();
    }

    static javax.transaction.Transaction getTx() {
        try {
            return tm.getTransaction();
        } catch (SystemException e) {
            throw new DatastoreFailureException("Cannot obtain tx.", e);
        }
    }

    static TransactionWrapper getTxWrapper(Transaction tx) {
        if (tx != null) {
            return new TransactionWrapper(getTx(), CapedwarfTransaction.class.cast(tx));
        } else {
            return null;
        }
    }

    static int getTxStatus() {
        try {
            return tm.getStatus();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    static void attach(TransactionWrapper tw) {
        if (tw == null)
            return;

        tw.setPrevious(suspendTx());

        resumeTx(tw.getDelegate());

        final CapedwarfTransaction tx = tw.getTransaction();

        Stack<CapedwarfTransaction> stack = current.get();
        if (stack == null) {
            stack = new Stack<CapedwarfTransaction>();
            current.set(stack);
        }

        stack.push(tx);
    }

    static void detach(TransactionWrapper tw) {
        if (tw == null)
            return;

        final Stack<CapedwarfTransaction> stack = current.get();
        if (stack == null || stack.isEmpty())
            throw new IllegalStateException("Illegal call to cleanup - stack should exist");

        stack.pop();

        if (stack.isEmpty()) {
            current.remove();
        }

        suspendTx();

        javax.transaction.Transaction previous = tw.getPrevious();
        if (previous != null) {
            resumeTx(previous);
        }
    }

    static javax.transaction.Transaction suspendTx() {
        try {
            return tm.suspend();
        } catch (SystemException e) {
            throw new DatastoreFailureException("Cannot suspend tx.", e);
        }
    }

    static void resumeTx(javax.transaction.Transaction transaction) {
        try {
            tm.resume(transaction);
        } catch (InvalidTransactionException e) {
            throw new DatastoreFailureException("Cannot resume tx.", e);
        } catch (SystemException e) {
            throw new DatastoreFailureException("Cannot resume tx.", e);
        }
    }

    private void checkIfCurrent() {
        if (getTransaction() != null)
            throw new IllegalStateException("Not current transaction -- other tx in progress!");
    }

    private void suspend() {
        try {
            transactions.set(tm.suspend());
        } catch (SystemException e) {
            throw new DatastoreFailureException("Cannot suspend tx.", e);
        }
    }

    private void resume(boolean ignoreException) {
        javax.transaction.Transaction t = getTransaction();
        try {
            transactions.remove(); // cleanup
            tm.resume(t);
        } catch (Exception e) {
            if (ignoreException == false)
                throw new DatastoreFailureException("Cannot resume tx.", e);
            else
                log.log(Level.SEVERE, "Failed to resume previous tx: " + t, e);
        }
    }

    static CapedwarfTransaction currentTransaction() {
        final Stack<CapedwarfTransaction> stack = current.get();
        return (stack != null) ? stack.peek() : null;
    }

    static TransactionOptions currentTransactionOptions() {
        final Stack<CapedwarfTransaction> stack = current.get();
        return (stack != null) ? stack.peek().options : null;
    }

    static boolean isXG() {
        final TransactionOptions to = currentTransactionOptions();
        return (to != null && to.isXG());
    }

    private CapedwarfTransaction cleanup(boolean resume) {
        final Stack<CapedwarfTransaction> stack = current.get();
        if (stack == null)
            throw new IllegalStateException("Illegal call to cleanup - stack should exist");

        final CapedwarfTransaction jt = stack.peek();
        if (jt != this)
            throw new IllegalArgumentException("Cannot cleanup non-current tx!");

        stack.pop(); // remove current

        CapedwarfTransaction previous = null;
        if (stack.isEmpty()) {
            current.remove();
        } else {
            previous = stack.peek();
            if (resume) {
                previous.resume(false); // resume previous
            }
        }
        return previous;
    }

    @SuppressWarnings({"unchecked"})
    static Collection<Transaction> getTransactions() {
        Stack stack = current.get();
        return (stack != null) ? Collections.unmodifiableCollection(stack) : Collections.<Transaction>emptyList();
    }

    public void commit() {
        checkIfCurrent();
        try {
            tm.commit();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot commit tx.", e);
        } finally {
            cleanup(true);
        }
    }

    public Future<Void> commitAsync() {
        checkIfCurrent();

        final CapedwarfTransaction previous = cleanup(false);
        final javax.transaction.Transaction tx = resumeAsync(previous);
        if (tx == null) {
            throw new IllegalArgumentException("No Tx -- should exist?!");
        }

        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                resumeTx(tx);
                try {
                    tx.commit();
                    return null;
                } finally {
                    suspendTx();
                }
            }
        });
    }

    public void rollback() {
        checkIfCurrent();
        try {
            tm.rollback();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot rollback tx.", e);
        } finally {
            cleanup(true);
        }
    }

    public Future<Void> rollbackAsync() {
        checkIfCurrent();

        final CapedwarfTransaction previous = cleanup(false);
        final javax.transaction.Transaction tx = resumeAsync(previous);
        if (tx == null) {
            throw new IllegalArgumentException("No Tx -- should exist?!");
        }

        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                resumeTx(tx);
                try {
                    tx.rollback();
                    return null;
                } finally {
                    suspendTx();
                }
            }
        });
    }

    private static javax.transaction.Transaction resumeAsync(CapedwarfTransaction previous) {
        final javax.transaction.Transaction tx = suspendTx(); // reset current thread
        if (previous != null) {
            previous.resume(false); // resume previous
        }
        return tx;
    }

    public String getId() {
        return EnvironmentFactory.getEnvironment().getTransactionId();
    }

    public String getApp() {
        return Application.getAppId();
    }

    public boolean isActive() {
        try {
            return (tm.getStatus() == Status.STATUS_ACTIVE);
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot check tx status.", e);
        }
    }
}
