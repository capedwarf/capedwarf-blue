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

import java.util.Collection;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

/**
 * Abstract base DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractDatastoreService implements BaseDatastoreService {
    private final BaseDatastoreService datastoreService;

    public AbstractDatastoreService(BaseDatastoreService datastoreService) {
        this.datastoreService = datastoreService;
    }

    BaseDatastoreService getDelegate() {
        return datastoreService;
    }

    public PreparedQuery prepare(Query query) {
        return getDelegate().prepare(query);
    }

    public PreparedQuery prepare(Transaction transaction, Query query) {
        return getDelegate().prepare(transaction, query);
    }

    public Transaction getCurrentTransaction() {
        return getDelegate().getCurrentTransaction();
    }

    public Transaction getCurrentTransaction(Transaction transaction) {
        return getDelegate().getCurrentTransaction(transaction);
    }

    public Collection<Transaction> getActiveTransactions() {
        return getDelegate().getActiveTransactions();
    }
}
