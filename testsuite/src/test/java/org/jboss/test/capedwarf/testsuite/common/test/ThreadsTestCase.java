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

package org.jboss.test.capedwarf.testsuite.common.test;

import java.util.Collections;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.testsuite.common.support.X;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class ThreadsTestCase extends BaseTest {
    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment().addClass(X.class);
    }

    @Test
    public void testThreadManager() throws Exception {
        X runnable = new X();
        Thread thread = ThreadManager.createBackgroundThread(runnable);
        thread.start();
        thread.join();
        Assert.assertEquals(1, runnable.x);
    }

    @Test
    public void testDS() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                DatastoreService service = DatastoreServiceFactory.getDatastoreService();
                Key key = service.put(new Entity("Threads"));
                Assert.assertNotNull(key);
                Entity entity = service.get(Collections.singleton(key)).get(key);
                Assert.assertNotNull(entity);
                service.delete(entity.getKey());
                Assert.assertTrue(service.get(Collections.singleton(key)).isEmpty());
            }
        };
        Thread thread = ThreadManager.createBackgroundThread(runnable);
        thread.start();
        thread.join();
    }

    @Test
    public void testDSWithTx() throws Exception {
        Runnable runnable = new Runnable() {
            public void run() {
                DatastoreService service = DatastoreServiceFactory.getDatastoreService();

                Transaction tx = service.beginTransaction();
                Key key = service.put(new Entity("Threads"));
                Assert.assertNotNull(key);
                tx.commit();

                Entity entity = service.get(Collections.singleton(key)).get(key);
                Assert.assertNotNull(entity);

                tx = service.beginTransaction();
                service.delete(entity.getKey());
                tx.commit();

                Assert.assertTrue(service.get(Collections.singleton(key)).isEmpty());
            }
        };
        Thread thread = ThreadManager.createBackgroundThread(runnable);
        thread.start();
        thread.join();
    }
}
