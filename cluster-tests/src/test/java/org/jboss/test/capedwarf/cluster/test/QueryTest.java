/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.cluster.test;

import java.util.concurrent.Callable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.capedwarf.datastore.ExposedDatastoreService;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class QueryTest extends ClusteredTestBase {

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void putInA() throws Exception {
        final Entity entity = new Entity("QT", 1);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().put(entity);
                return null;
            }

            @Override
            public String toString() {
                return "putInA";
            }
        });
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void deleteInB() throws Exception {
        final Entity entity = getService().get(KeyFactory.createKey("QT", 1));
        Assert.assertNotNull(entity);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().delete(entity.getKey());
                return null;
            }

            @Override
            public String toString() {
                return "deleteInB";
            }
        });
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void queryInA() throws Exception {
        Query query = new Query("QT");
        for (Entity e : getService().prepare(query).asIterable(FetchOptions.Builder.withChunkSize(10))) {
            Assert.fail("Should not be here: " + e);
        }
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep2")
    public void putInB() throws Exception {
        final Entity entity = new Entity("QT", 2);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().put(entity);
                return null;
            }

            @Override
            public String toString() {
                return "putInB";
            }
        });
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    @InSequence(50)
    @Test
    @OperateOnDeployment("dep1")
    public void deleteAndQueryInA() throws Exception {
        final Entity entity = getService().get(KeyFactory.createKey("QT", 2));
        Assert.assertNotNull(entity);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().delete(entity.getKey());
                return null;
            }

            @Override
            public String toString() {
                return "deleteAndQueryInA";
            }
        });

        //TODO remove when sync indexing implemented
        //sync();

        Query query = new Query("QT");
        for (Entity e : getService().prepare(query).asIterable(FetchOptions.Builder.withChunkSize(10))) {
            Assert.fail("Should not be here: " + e);
        }
    }

    @InSequence(60)
    @Test
    @OperateOnDeployment("dep2")
    public void putInB_2() throws Exception {
        final Entity entity = new Entity("QT", 3);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().put(entity);
                return null;
            }

            @Override
            public String toString() {
                return "putInB_2";
            }
        });
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    @InSequence(70)
    @Test
    @OperateOnDeployment("dep1")
    public void deleteAndQueryInA_2() throws Exception {
        final Entity entity = getService().get(KeyFactory.createKey("QT", 3));
        Assert.assertNotNull(entity);
        wrap(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getService().delete(entity.getKey());
                return null;
            }

            @Override
            public String toString() {
                return "deleteAndQueryInA_2";
            }
        });

        //TODO remove when sync indexing implemented
        //sync();

        Query query = new Query("QT");
        for (Entity e : getService().prepare(query).asIterable(FetchOptions.Builder.withChunkSize(10))) {
            Assert.fail("Should not be here: " + e);
        }
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void tearDownDepA() throws Exception {
        tearDown();
    }

    @InSequence(2000)
    @Test
    @OperateOnDeployment("dep2")
    public void tearDownDepB() throws Exception {
        tearDown();
    }

    private static <T> T wrap(Callable<T> callable) throws Exception {
        long now = System.currentTimeMillis();
        T result = callable.call();
        System.err.println(callable + " = " + (System.currentTimeMillis() - now) + "ms");
        return result;
    }

    private DatastoreService getService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private void tearDown() {
        ((ExposedDatastoreService) getService()).clearCache();
    }
}
