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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossDatastoreService extends AbstractDatastoreService implements DatastoreService {
    public JBossDatastoreService() {
        this(null);
    }

    public JBossDatastoreService(DatastoreServiceConfig config) {
        super(new JBossAsyncDatastoreService(config));
    }

    protected <T> T unwrap(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    JBossAsyncDatastoreService getDelegate() {
        return (JBossAsyncDatastoreService) super.getDelegate();
    }
    
    public Entity get(Key key) throws EntityNotFoundException {
        return unwrap(getDelegate().get(key));
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        return unwrap(getDelegate().get(transaction, key));
    }

    public Map<Key, Entity> get(Iterable<Key> keys) {
        return unwrap(getDelegate().get(keys));
    }

    public Map<Key, Entity> get(Transaction transaction, Iterable<Key> keys) {
        return unwrap(getDelegate().get(transaction, keys));
    }

    public Key put(Entity entity) {
        return unwrap(getDelegate().put(entity));
    }

    public Key put(Transaction transaction, Entity entity) {
        return unwrap(getDelegate().put(transaction, entity));
    }

    public List<Key> put(Iterable<Entity> entities) {
        return unwrap(getDelegate().put(entities));
    }

    public List<Key> put(Transaction transaction, Iterable<Entity> entities) {
        return unwrap(getDelegate().put(transaction, entities));
    }

    public void delete(Key... keys) {
        unwrap(getDelegate().delete(keys));
    }

    public void delete(Transaction transaction, Key... keys) {
        unwrap(getDelegate().delete(transaction, keys));
    }

    public void delete(Iterable<Key> keys) {
        unwrap(getDelegate().delete(keys));
    }

    public void delete(Transaction transaction, Iterable<Key> keys) {
        unwrap(getDelegate().delete(transaction, keys));
    }

    public Transaction beginTransaction() {
        return unwrap(getDelegate().beginTransaction());
    }

    public Transaction beginTransaction(TransactionOptions transactionOptions) {
        return unwrap(getDelegate().beginTransaction(transactionOptions));
    }

    public KeyRange allocateIds(String kind, long num) {
        return unwrap(getDelegate().allocateIds(kind, num));
    }

    public KeyRange allocateIds(Key key, String s, long l) {
        return unwrap(getDelegate().allocateIds(key, s, l));
    }

    public KeyRangeState allocateIdRange(KeyRange keys) {
        return getDelegate().getDelegate().allocateIdRange(keys);
    }

    public DatastoreAttributes getDatastoreAttributes() {
        return unwrap(getDelegate().getDatastoreAttributes());
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return unwrap(getDelegate().getIndexes());
    }

    /**
     * Testing only!
     */
    public void clearCache() {
        getDelegate().clearCache();
    }
}
