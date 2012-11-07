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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SyncCallbacksTestCase extends AbstractCallbacksTest {
    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Test
    public void testSmoke() throws Exception {
        DatastoreService service = createDatastoreService();

        Key k = service.put(new Entity(KIND));
        assertCallbackInvoked("PrePut", "PostPut");

        service.get(k);
        assertCallbackInvoked("PreGet", "PostLoad");

        PreparedQuery pq = service.prepare(new Query(KIND));
        assertCallbackInvoked("PreQuery");

        List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        list.get(0);
        assertCallbackInvokedAtLeastOnce("PostLoad");


        service.delete(k);
        assertCallbackInvoked("PreDelete", "PostDelete");
    }

    @Test
    public void testSmokeWithTx() throws Exception {
        DatastoreService service = createDatastoreService();

        Key k;
        Transaction tx = service.beginTransaction();
        try {
            k = service.put(new Entity(KIND));
            assertCallbackInvoked("PrePut");
            tx.commit();
            assertCallbackInvoked("PostPut");
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx = service.beginTransaction();
        try {
            service.get(k);
            assertCallbackInvoked("PreGet", "PostLoad");

            PreparedQuery pq = service.prepare(new Query(KIND));
            assertCallbackInvoked("PreQuery");
            List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
            list.get(0);
            assertCallbackInvokedAtLeastOnce("PostLoad");

            service.delete(k);
            assertCallbackInvoked("PreDelete");
            tx.commit();
            assertCallbackInvoked("PostDelete");
        } catch (Exception ex) {
            tx.rollback();
        }
    }


    @Test
    public void testBatch() throws Exception {
        DatastoreService service = createDatastoreService();

        List<Entity> entities = Arrays.asList(new Entity(KIND, "first"), new Entity(KIND, "second"), new Entity(KIND, "third"));

        List<Key> keys = service.put(entities);
        assertCallbackInvoked("PrePut", "PrePut", "PrePut", "PostPut", "PostPut", "PostPut");

        service.get(keys);
        assertCallbackInvoked("PreGet", "PreGet", "PreGet", "PostLoad", "PostLoad", "PostLoad");

        PreparedQuery pq = service.prepare(new Query(KIND));
        assertCallbackInvoked("PreQuery");
        List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
        list.get(0);
        assertCallbackInvokedAtLeastOnce("PostLoad");

        service.delete(keys);
        assertCallbackInvoked("PreDelete", "PreDelete", "PreDelete", "PostDelete", "PostDelete", "PostDelete");
    }


    @Test
    public void testBatchWithTx() throws Exception {
        DatastoreService service = createDatastoreService();

        List<Key> keys;
        Transaction tx = service.beginTransaction();
        try {
            Key first = KeyFactory.createKey(KIND, "first");
            keys = service.put(Arrays.asList(new Entity(first), new Entity(KIND, "second", first), new Entity(KIND, "third", first)));
            assertCallbackInvoked("PrePut", "PrePut", "PrePut");

            tx.commit();
            assertCallbackInvoked("PostPut", "PostPut", "PostPut");
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }

        tx = service.beginTransaction();
        try {
            service.get(keys);
            assertCallbackInvoked("PreGet", "PreGet", "PreGet", "PostLoad", "PostLoad", "PostLoad");

            PreparedQuery pq = service.prepare(new Query(KIND));
            assertCallbackInvoked("PreQuery");
            List<Entity> list = pq.asList(FetchOptions.Builder.withDefaults());
            list.get(0);
            assertCallbackInvokedAtLeastOnce("PostLoad");

            service.delete(keys);
            assertCallbackInvoked("PreDelete", "PreDelete", "PreDelete");

            tx.commit();
            assertCallbackInvoked("PostDelete", "PostDelete", "PostDelete");
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        }
    }


}
