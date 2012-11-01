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

import java.util.Map;

import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
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
     * Get.
     *
     * @param tx current tx
     * @param key key
     * @return entity
     * @throws EntityNotFoundException
     */
    Entity get(Transaction tx, Key key) throws EntityNotFoundException;

    /**
     * Put.
     *
     * @param tx current tx
     * @param entity entities
     * @return key
     */
    Key put(Transaction tx, Entity entity);

    /**
     * Delete.
     *
     * @param tx current tx
     * @param key key
     */
    void delete(Transaction tx, Key key);

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
     * Clear cache.
     */
    void clearCache();
}
