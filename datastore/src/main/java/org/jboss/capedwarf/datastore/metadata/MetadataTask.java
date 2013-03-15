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

package org.jboss.capedwarf.datastore.metadata;

import javax.transaction.TransactionManager;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.jboss.capedwarf.common.tx.AbstractTxCallable;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class MetadataTask extends AbstractTxCallable<Void> {
    protected final String value;
    private final boolean add;

    public MetadataTask(String value, boolean add) {
        this.value = value;
        this.add = add;
    }

    protected TransactionManager getTransactionManager() {
        return ComponentRegistry.getInstance().getComponent(Keys.TM);
    }

    protected Void callInTx() throws Exception {

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        final String previousNS = NamespaceManager.get();
        NamespaceManager.set(getNamespace());
        try {
            Key key = createKey();

            if (add) {
                ds.put(new Entity(key));
            } else {
                ds.delete(key);
            }
        } finally {
            NamespaceManager.set(previousNS);
        }

        return null;
    }

    protected abstract String getNamespace();

    protected abstract Key createKey();
}