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

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

/**
 * JBoss GAE transaction.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossTransaction implements Transaction {
    private static final Logger log = Logger.getLogger(JBossTransaction.class.getName());

    private final static TransactionManager tm = JndiLookupUtils.lookup("tm.jndi.name", TransactionManager.class, "java:jboss/TransactionManager");
    private final static ThreadLocal<Stack<JBossTransaction>> current = new ThreadLocal<Stack<JBossTransaction>>();

    private javax.transaction.Transaction transaction;

    private JBossTransaction() {
    }

    static Transaction newTransaction() {
        Stack<JBossTransaction> stack = current.get();
        if (stack == null) {
            stack = new Stack<JBossTransaction>();
            current.set(stack);
        } else {
            JBossTransaction jt = stack.peek();
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
        JBossTransaction tx = new JBossTransaction();
        stack.push(tx);
        return tx;
    }

    private void suspend() {
        try {
            transaction = tm.suspend();
        } catch (SystemException e) {
            throw new DatastoreFailureException("Cannot suspend tx.", e);
        }
    }

    private void resume(boolean ignoreException) {
        javax.transaction.Transaction t = transaction;
        try {
            transaction = null; // cleanup
            tm.resume(t);
        } catch (Exception e) {
            if (ignoreException == false)
                throw new DatastoreFailureException("Cannot resume tx.", e);
            else
                log.warning("Failed to resume previous tx: " + t);
        }
    }

    static Transaction currentTransaction() {
        Stack<JBossTransaction> stack = current.get();
        return (stack != null) ? stack.peek() : null;
    }

    private static void cleanup(JBossTransaction tx) {
        Stack<JBossTransaction> stack = current.get();
        if (stack == null)
            throw new IllegalStateException("Illegal call to cleanup - stack should exist");

        JBossTransaction jt = stack.peek();
        if (jt != tx)
            throw new IllegalArgumentException("Cannot cleanup non-current tx!");

        stack.pop(); // remove current

        if (stack.isEmpty())
            current.remove();
        else
            stack.peek().resume(false); // resume previous
    }

    @SuppressWarnings({"unchecked"})
    static Collection<Transaction> getTransactions() {
        Stack stack = current.get();
        return (stack != null) ? Collections.unmodifiableCollection(stack) : Collections.<Transaction>emptyList();
    }

    public void commit() {
        if (transaction != null)
            throw new IllegalStateException("Not current transaction -- other tx in progress!");

        try {
            tm.commit();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot commit tx.", e);
        } finally {
            cleanup(this);
        }
    }

    public Future<Void> commitAsync() {
        FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
            public Void call() throws Exception {
                commit();
                return null;
            }
        });
        final Executor executor = ExecutorFactory.getInstance();
        executor.execute(task);
        return task;
    }

    public void rollback() {
        if (transaction != null)
            throw new IllegalStateException("Not current transaction -- other tx in progress!");

        try {
            tm.rollback();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot rollback tx.", e);
        } finally {
            cleanup(this);
        }
    }

    public Future<Void> rollbackAsync() {
        FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
            public Void call() throws Exception {
                rollback();
                return null;
            }
        });
        final Executor executor = ExecutorFactory.getInstance();
        executor.execute(task);
        return task;
    }

    public String getId() {
        return null; // TODO
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
