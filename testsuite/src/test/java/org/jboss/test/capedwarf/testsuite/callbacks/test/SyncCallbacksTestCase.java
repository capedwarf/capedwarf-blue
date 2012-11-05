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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

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

        reset();

        Key k = service.put(new Entity(SyncCallbackHandler.KIND));
        Assert.assertEquals(Arrays.asList("PrePut", "PostPut"), SyncCallbackHandler.states);
        reset();

        service.get(k);
        Assert.assertEquals(Arrays.asList("PreGet", "PostLoad"), SyncCallbackHandler.states);
        reset();

        PreparedQuery pq = service.prepare(new Query(SyncCallbackHandler.KIND));
        Assert.assertEquals(Arrays.asList("PreQuery"), SyncCallbackHandler.states);
        pq.asList(FetchOptions.Builder.withDefaults());
        reset();

        service.delete(k);
        Assert.assertEquals(Arrays.asList("PreDelete", "PostDelete"), SyncCallbackHandler.states);
        reset();
    }

    @Test
    public void testSmokeWithTx() throws Exception {
        DatastoreService service = createDatastoreService();

        reset();

        boolean ok = false;
        Key k;
        Transaction tx = service.beginTransaction();
        try {
            k = service.put(new Entity(SyncCallbackHandler.KIND));
            Assert.assertEquals(Arrays.asList("PrePut"), SyncCallbackHandler.states);
            reset();

            ok = true;
        } finally {
            if (ok)
                tx.commit();
            else
                tx.rollback();
        }
        Assert.assertEquals(Arrays.asList("PostPut"), SyncCallbackHandler.states);
        reset();

        ok = false;
        tx = service.beginTransaction();
        try {
            service.get(k);
            Assert.assertEquals(Arrays.asList("PreGet", "PostLoad"), SyncCallbackHandler.states);
            reset();

            PreparedQuery pq = service.prepare(new Query(SyncCallbackHandler.KIND));
            Assert.assertEquals(Arrays.asList("PreQuery"), SyncCallbackHandler.states);
            pq.asList(FetchOptions.Builder.withDefaults());
            reset();

            service.delete(k);
            Assert.assertEquals(Arrays.asList("PreDelete"), SyncCallbackHandler.states);
            reset();

            ok = true;
        } finally {
            if (ok)
                tx.commit();
            else
                tx.rollback();
        }
        Assert.assertEquals(Arrays.asList("PostDelete"), SyncCallbackHandler.states);
        reset();
    }
}
