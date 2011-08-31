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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;

import java.util.List;
import java.util.Map;

/**
 * JBoss DatastoreService impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossDatastoreService extends AbstractDatastoreService implements DatastoreService {
    public Entity get(Key key) throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Entity get(Transaction transaction, Key key) throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<Key, Entity> get(Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<Key, Entity> get(Transaction transaction, Iterable<Key> keyIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Key put(Entity entity) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Key put(Transaction transaction, Entity entity) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Key> put(Iterable<Entity> entityIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Key> put(Transaction transaction, Iterable<Entity> entityIterable) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Key... keys) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Transaction transaction, Key... keys) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Iterable<Key> keyIterable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Transaction transaction, Iterable<Key> keyIterable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Transaction beginTransaction() {
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
