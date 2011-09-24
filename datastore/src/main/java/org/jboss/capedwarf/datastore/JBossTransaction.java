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
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * JBoss GAE transaction.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossTransaction implements Transaction {

    private final TransactionManager tm;

    public JBossTransaction() {
        try {
            tm = JndiLookupUtils.lookup("tm.jndi.name", TransactionManager.class, "java:jboss/TransactionManager");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            tm.commit();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot commit tx.", e);
        }
    }

    public Future<Void> commitAsync() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void rollback() {
        try {
            tm.rollback();
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot rollback tx.", e);
        }
    }

    public Future<Void> rollbackAsync() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getApp() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isActive() {
        try {
            return (tm.getStatus() == Status.STATUS_ACTIVE);
        } catch (Exception e) {
            throw new DatastoreFailureException("Cannot check tx status.", e);
        }
    }
}
