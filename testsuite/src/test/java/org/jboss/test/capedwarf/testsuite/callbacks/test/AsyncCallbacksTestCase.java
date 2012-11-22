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

package org.jboss.test.capedwarf.testsuite.callbacks.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Category(All.class)
public class AsyncCallbacksTestCase extends AbstractCallbacksTest {
    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Test
    public void testSmoke() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        Future<Key> f = service.put(new Entity(KIND));
        assertCallbackInvoked("PrePut");

        Key k = f.get();
        assertCallbackInvoked("PostPut");

        Future<Entity> e = service.get(k);
        assertCallbackInvoked("PreGet");

        e.get();
        assertCallbackInvoked("PostLoad");

        PreparedQuery pq = service.prepare(new Query(KIND));
        assertCallbackInvoked("PreQuery");

        List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        list.get(0);
        assertCallbackInvokedAtLeastOnce("PostLoad");

        Future<Void> v = service.delete(k);
        assertCallbackInvoked("PreDelete");

        v.get();
        assertCallbackInvoked("PostDelete");
    }

    @Test
    public void testSmokeWithTx() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        Key key;
        Future<Key> putFuture;
        Transaction tx = service.beginTransaction().get();
        try {
            putFuture = service.put(new Entity(KIND));
            assertCallbackInvoked("PrePut");
            putFuture.get();
            assertNoCallbackInvoked();

            tx.commit();
            assertCallbackInvoked("PostPut");
            key = putFuture.get();
            assertNoCallbackInvoked();

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx = service.beginTransaction().get();
        Future<Void> deleteFuture;
        try {
            Future<Entity> getFuture = service.get(key);
            assertCallbackInvoked("PreGet");
            getFuture.get();
            assertCallbackInvoked("PostLoad");

            PreparedQuery pq = service.prepare(new Query(KIND));
            assertCallbackInvoked("PreQuery");
            List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
            list.get(0);
            assertCallbackInvokedAtLeastOnce("PostLoad");

            deleteFuture = service.delete(key);
            assertCallbackInvoked("PreDelete");
            deleteFuture.get();
            assertNoCallbackInvoked();

            tx.commit();
            assertCallbackInvoked("PostDelete");
            deleteFuture.get();
            assertNoCallbackInvoked();

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }
    }

    @Test
    public void testBatch() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        List<Entity> entities = Arrays.asList(new Entity(KIND, "first"), new Entity(KIND, "second"), new Entity(KIND, "third"));

        Future<List<Key>> putFuture = service.put(entities);
        assertCallbackInvoked("PrePut", "PrePut", "PrePut");

        List<Key> keys = putFuture.get();
        assertCallbackInvoked("PostPut", "PostPut", "PostPut");

        Future<Map<Key, Entity>> getFuture = service.get(keys);
        assertCallbackInvoked("PreGet", "PreGet", "PreGet");

        getFuture.get();
        assertCallbackInvoked("PostLoad", "PostLoad", "PostLoad");

        PreparedQuery pq = service.prepare(new Query(KIND));
        assertCallbackInvoked("PreQuery");

        List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        list.get(0);
        assertCallbackInvokedAtLeastOnce("PostLoad");

        Future<Void> deleteFuture = service.delete(keys);
        assertCallbackInvoked("PreDelete", "PreDelete", "PreDelete");

        deleteFuture.get();
        assertCallbackInvoked("PostDelete", "PostDelete", "PostDelete");
    }

    @Test
    public void testBatchWithTx() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        List<Key> keys;
        Future<List<Key>> putFuture;
        Transaction tx = service.beginTransaction().get();
        try {
            Key first = KeyFactory.createKey(KIND, "first");
            List<Entity> entities = Arrays.asList(new Entity(first), new Entity(KIND, "second", first), new Entity(KIND, "third", first));

            putFuture = service.put(entities);
            assertCallbackInvoked("PrePut", "PrePut", "PrePut");

            putFuture.get();
            assertNoCallbackInvoked();

            tx.commit();
            assertCallbackInvoked("PostPut", "PostPut", "PostPut");

            keys = putFuture.get();
            assertNoCallbackInvoked();

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx = service.beginTransaction().get();
        Future<Void> deleteFuture;
        try {
            Future<Map<Key, Entity>> getFuture = service.get(keys);
            assertCallbackInvoked("PreGet", "PreGet", "PreGet");

            getFuture.get();
            assertCallbackInvoked("PostLoad", "PostLoad", "PostLoad");

            PreparedQuery pq = service.prepare(new Query(KIND));
            assertCallbackInvoked("PreQuery");
            List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
            list.get(0);
            assertCallbackInvokedAtLeastOnce("PostLoad");

            deleteFuture = service.delete(keys);
            assertCallbackInvoked("PreDelete", "PreDelete", "PreDelete");

            deleteFuture.get();
            assertNoCallbackInvoked();

            tx.commit();
            assertCallbackInvoked("PostDelete", "PostDelete", "PostDelete");

            deleteFuture.get();
            assertNoCallbackInvoked();

        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }
    }

    // TODO: test if sync commit waits for background tasks to finish

}
