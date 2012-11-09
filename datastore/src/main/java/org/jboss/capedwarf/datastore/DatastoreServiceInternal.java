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

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

/**
 * DatastoreService impl SPI.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
interface DatastoreServiceInternal extends BaseDatastoreService {
    /**
     * Get datastore config.
     *
     * @return config
     */
    DatastoreServiceConfig getDatastoreServiceConfig();

    /**
     * Get.
     *
     * @param tx current tx
     * @param key key
     * @return entity
     */
    Entity get(Transaction tx, Key key);

    /**
     * Put.
     *
     * @param tx current tx
     * @param entity entities
     * @param post the post fn
     * @return key
     */
    Key put(Transaction tx, Entity entity, Runnable post);

    /**
     * Put.
     *
     * @param tx current tx
     * @param entities entities
     * @param post the post fn
     * @return keys
     */
    List<Key> put(Transaction tx, Iterable<Entity> entities, Runnable post);

    /**
     * Delete.
     *
     * @param tx current tx
     * @param keys keys
     * @param post the post fn
     */
    void delete(Transaction tx, Iterable<Key> keys, Runnable post);

    /**
     * Begin tx.
     *
     * @param options tx options
     * @return tx
     */
    Transaction beginTransaction(TransactionOptions options);

    /**
     * Get indexes.
     *
     * @return indexes
     */
    Map<Index, Index.IndexState> getIndexes();

    /**
     * Allocate ids.
     *
     * @param parent parent key
     * @param kind the kind
     * @param num the number of ids
     * @return key range
     */
    KeyRange allocateIds(Key parent, String kind, long num);

    /**
     * Allocate id range.
     *
     * @param keyRange key range
     * @return key range state
     */
    DatastoreService.KeyRangeState allocateIdRange(KeyRange keyRange);

    /**
     * Get datastore attributes.
     *
     * @return datastore attributes
     */
    DatastoreAttributes getDatastoreAttributes();

    /**
     * Get callbacks.
     *
     * @return callbacks
     */
    DatastoreCallbacks getDatastoreCallbacks();

    /**
     * Clear cache.
     */
    void clearCache();
}
