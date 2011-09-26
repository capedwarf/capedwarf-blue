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

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossDatastoreService extends AbstractDatastoreService implements DatastoreService {

    public Entity get(Key key) throws EntityNotFoundException {
        Entity entity = store.get(key);
        if (entity == null)
            throw new EntityNotFoundException(key);
        else
            return entity;
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        return get(key);
    }

    public Map<Key, Entity> get(Iterable<Key> keyIterable) {
        Map<Key, Entity> result = new HashMap<Key, Entity>();
        for (Key key : keyIterable)
            result.put(key, store.get(key));
        return result;
    }

    public Map<Key, Entity> get(Transaction transaction, Iterable<Key> keyIterable) {
        return get(keyIterable);
    }

    public Key put(Entity entity) {
        Entity v = store.put(entity.getKey(), entity);
        return v == null ? null : v.getKey();
    }

    public Key put(Transaction transaction, Entity entity) {
        return put(entity);
    }

    public List<Key> put(Iterable<Entity> entityIterable) {
        List<Key> list = new ArrayList<Key>();
        for (Entity e : entityIterable) {
            Key key = put(e);
            list.add(key);
        }
        return list;
    }

    public List<Key> put(Transaction transaction, Iterable<Entity> entityIterable) {
        return put(entityIterable);
    }

    public void delete(Key... keys) {
        delete(Arrays.asList(keys));
    }

    public void delete(Transaction transaction, Key... keys) {
        delete(keys);
    }

    public void delete(Iterable<Key> keyIterable) {
        for (Key key : keyIterable)
            store.remove(key);
    }

    public void delete(Transaction transaction, Iterable<Key> keyIterable) {
        delete(keyIterable);
    }

    public Map<Index, Index.IndexState> getIndexes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Transaction beginTransaction() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Transaction beginTransaction(TransactionOptions transactionOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public KeyRange allocateIds(String s, long l) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public KeyRange allocateIds(Key key, String s, long l) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public KeyRangeState allocateIdRange(KeyRange keyRange) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DatastoreAttributes getDatastoreAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
