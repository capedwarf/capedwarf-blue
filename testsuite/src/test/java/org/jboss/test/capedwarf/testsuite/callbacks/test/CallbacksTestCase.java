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

import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
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
public class CallbacksTestCase extends AbstractCallbacksTest {
    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    protected AsyncDatastoreService createAsyncDatastoreService() {
        return DatastoreServiceFactory.getAsyncDatastoreService();
    }

    @Test
    public void testSmoke() throws Exception {
        reset();

        Future<Key> f = service.put(new Entity(CallbackHandler.KIND));
        Assert.assertEquals("PrePut", CallbackHandler.state);
        Key k = f.get();
        Assert.assertEquals("PostPut", CallbackHandler.state);

        Future<Entity> e = service.get(k);
        Assert.assertEquals("PreGet", CallbackHandler.state);
        e.get();
        Assert.assertEquals("PostLoad", CallbackHandler.state);

        PreparedQuery pq = service.prepare(new Query(CallbackHandler.KIND));
        Assert.assertEquals("PreQuery", CallbackHandler.state);
        pq.asList(FetchOptions.Builder.withDefaults());

        Future<Void> v = service.delete(k);
        Assert.assertEquals("PreDelete", CallbackHandler.state);
        v.get();
        Assert.assertEquals("PostDelete", CallbackHandler.state);
    }

    @Test
    public void testSmokeWithTx() throws Exception {
        reset();

        boolean ok = false;
        Key k;
        Future<Key> f;
        Transaction tx = service.beginTransaction().get();
        try {
            f = service.put(new Entity(CallbackHandler.KIND));
            Assert.assertEquals("PrePut", CallbackHandler.state);
            f.get();
            Assert.assertEquals("PrePut", CallbackHandler.state);

            ok = true;
        } finally {
            Future<Void> r = (ok ? tx.commitAsync() : tx.rollbackAsync());
            r.get();
        }
        k = f.get();
        Assert.assertEquals("PostPut", CallbackHandler.state);

        ok = false;
        tx = service.beginTransaction().get();
        Future<Void> v;
        try {
            Future<Entity> e = service.get(k);
            Assert.assertEquals("PreGet", CallbackHandler.state);
            e.get();
            Assert.assertEquals("PostLoad", CallbackHandler.state);

            PreparedQuery pq = service.prepare(new Query(CallbackHandler.KIND));
            Assert.assertEquals("PreQuery", CallbackHandler.state);
            pq.asList(FetchOptions.Builder.withDefaults());

            v = service.delete(k);
            Assert.assertEquals("PreDelete", CallbackHandler.state);
            v.get();
            // should still be pre
            Assert.assertEquals("PreDelete", CallbackHandler.state);

            ok = true;
        } finally {
            Future<Void> r = (ok ? tx.commitAsync() : tx.rollbackAsync());
            r.get();
        }
        v.get();
        Assert.assertEquals("PostDelete", CallbackHandler.state);
    }
}
