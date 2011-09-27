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

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * JBoss async DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossAsyncDatastoreService extends AbstractDatastoreService implements AsyncDatastoreService {
    public Future<Transaction> beginTransaction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Transaction> beginTransaction(TransactionOptions transactionOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Entity> get(Key key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Entity> get(Transaction transaction, Key key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Map<Key, Entity>> get(Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Map<Key, Entity>> get(Transaction transaction, Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Key> put(Entity entity) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Key> put(Transaction transaction, Entity entity) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<List<Key>> put(Iterable<Entity> entityIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<List<Key>> put(Transaction transaction, Iterable<Entity> entityIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> delete(Key... keys) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> delete(Transaction transaction, Key... keys) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> delete(Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> delete(Transaction transaction, Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<KeyRange> allocateIds(String s, long l) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<KeyRange> allocateIds(Key key, String s, long l) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<DatastoreAttributes> getDatastoreAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Map<Index, Index.IndexState>> getIndexes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
