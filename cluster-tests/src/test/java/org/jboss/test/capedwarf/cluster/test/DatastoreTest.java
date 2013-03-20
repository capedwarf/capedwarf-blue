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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;
import org.jboss.capedwarf.datastore.ExposedDatastoreService;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class DatastoreTest extends ClusteredTestBase {

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void putStoresEntityOnDepA() throws Exception {
        Entity entity = createTestEntity("KIND", 1);
        getService().put(entity);
        assertStoreContains(entity);
    }

    @InSequence(31)
    @Test
    @OperateOnDeployment("dep2")
    public void putStoresEntityOnDepB() throws Exception {
        Entity entity = createTestEntity("KIND", 2);
        getService().put(entity);
        assertStoreContains(entity);
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep1")
    public void getEntityOnDepA() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("KIND", 1);
        Entity lookup = getService().get(key);

        Assert.assertNotNull(lookup);

        Entity entity = createTestEntity("KIND", 1);
        Assert.assertEquals(entity, lookup);
    }

    @InSequence(50)
    @Test
    @OperateOnDeployment("dep2")
    public void getEntityOnDepB() throws Exception {
        waitForSync();

        Entity entity = createTestEntity("KIND", 1);
        assertStoreContains(entity);
    }

    @InSequence(52)
    @Test
    @OperateOnDeployment("dep1")
    public void queryOnA() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("KIND")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(53)
    @Test
    @OperateOnDeployment("dep2")
    public void queryOnB() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("KIND")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);

        getService().delete(KeyFactory.createKey("KIND", 1));
        getService().delete(KeyFactory.createKey("KIND", 2));
    }

    @InSequence(100)
    @Test
    @OperateOnDeployment("dep1")
    public void indexGenAndQueryInsertOnA() throws Exception {
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "A");
        Key key = getService().put(entity);

        Assert.assertNotNull(getService().get(KeyFactory.createKey("indexGen", key.getId())));

        Entity entity2 = new Entity("indexGen");
        entity2.setProperty("text", "A1");
        getService().put(entity2);

        waitForSync();

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(2, count);
    }

    @InSequence(110)
    @Test
    @OperateOnDeployment("dep2")
    public void indexGenAndQueryInsertOnB() throws Exception {
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "B");
        getService().put(entity);

        waitForSync();

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(3, count);
    }

    @InSequence(120)
    @Test
    @OperateOnDeployment("dep1")
    public void testContentOnA() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(3, count);

        List<Entity> list = getService().prepare(new Query("indexGen")).asList(Builder.withDefaults());
        Assert.assertEquals(3, list.size());

        List<String> cached = new ArrayList<String>();
        for (Entity entity : list) {
            cached.add((String) entity.getProperty("text"));
        }
        Assert.assertTrue(cached.contains("A"));
        Assert.assertTrue(cached.contains("A1"));
        Assert.assertTrue(cached.contains("B"));
    }

    @InSequence(130)
    @Test
    @OperateOnDeployment("dep2")
    public void testContentOnB() throws Exception {
        waitForSync();

        List<Entity> list = getService().prepare(new Query("indexGen")).asList(Builder.withDefaults());
        Assert.assertEquals(3, list.size());

        List<String> cached = new ArrayList<String>();
        for (Entity entity : list) {
            cached.add((String) entity.getProperty("text"));
        }

        Assert.assertTrue(cached.contains("A"));
        Assert.assertTrue(cached.contains("A1"));
        Assert.assertTrue(cached.contains("B"));

        //cleanup "indexGen" entities
        for (Entity entity : list) {
            getService().delete(entity.getKey());
        }

    }

    @Category(JBoss.class)
    @InSequence(200)
    @Test
    @OperateOnDeployment("dep1")
    public void testKeySerializationOndepA() throws Exception {
        Key key = KeyFactory.createKey("KIND", 1);
        Entity entity = new Entity(key);
        entity.setProperty("name", "exist" + new Date());
        getService().put(entity);
    }

    @Category(JBoss.class)
    @InSequence(210)
    @Test
    @OperateOnDeployment("dep2")
    public void testKeySerializationOndepB() throws Exception {
        TargetInvocation<Object> isChecked = ReflectionUtils.cacheInvocation(Key.class, "isChecked");

        waitForSync();
        Key key = KeyFactory.createKey("KIND", 1);
        Entity entity = getService().get(key);
        Assert.assertTrue((Boolean)isChecked.invoke(entity.getKey()));

        getService().delete(entity.getKey());
    }

    @InSequence(300)
    @Test
    @OperateOnDeployment("dep1")
    public void testVersionOndepA() throws Exception {
        Entity entity = new Entity("VersionTest", "name");
        // 3 puts
        AtomicLong counter = new AtomicLong(1);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
    }


    @InSequence(310)
    @Test
    @OperateOnDeployment("dep2")
    public void testVersionOndepB() throws Exception {
        waitForSync();
        Entity entity = getService().get(KeyFactory.createKey("VersionTest", "name"));
        try {
            // 3 puts
            AtomicLong counter = new AtomicLong(4);
            assertVersion(entity, counter);
            assertVersion(entity, counter);
            assertVersion(entity, counter);
        } finally {
            // cleanup
            getService().delete(entity.getKey());
        }
    }

    @InSequence(320)
    @Test
    @OperateOnDeployment("dep1")
    public void testVersionWithIdOndepA() throws Exception {
        Entity entity = new Entity(KeyFactory.createKey("versioned", 1));
        // 3 puts
        AtomicLong counter = new AtomicLong(1);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
        assertVersion(entity, counter);

        getService().delete(entity.getKey());
        waitForSync();

        entity = new Entity(KeyFactory.createKey("versioned", 1));
        // 3 puts
        counter = new AtomicLong(1);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
    }


    @InSequence(330)
    @Test
    @OperateOnDeployment("dep2")
    public void testVersionWithIdOndepB() throws Exception {
        waitForSync();
        Entity entity = getService().get(KeyFactory.createKey("versioned", 1));
        // 3 puts
        AtomicLong counter = new AtomicLong(4);
        assertVersion(entity, counter);
        assertVersion(entity, counter);
        assertVersion(entity, counter);

        getService().delete(entity.getKey());
    }


    @InSequence(340)
    @Test
    @OperateOnDeployment("dep1")
    public void testQueryOnDepA() throws Exception {
        Entity e1 = new Entity("VT1");
        AtomicLong counter = new AtomicLong(1);
        assertVersion(e1, counter);
        assertVersion(e1, counter);

        Query query = new Query("VT1");
        query.setFilter(new Query.FilterPredicate(Entity.VERSION_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN, 0L));
        List<Entity> all = getService().prepare(query).asList(FetchOptions.Builder.withDefaults());
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(e1, all.get(0));
    }


    @InSequence(350)
    @Test
    @OperateOnDeployment("dep2")
    public void testQueryOnDepB() throws Exception {
        waitForSync();
        Entity e2 = new Entity("VT1");
        AtomicLong counter = new AtomicLong(1);
        assertVersion(e2, counter);

        Query query = new Query("VT1");
        query.setFilter(new Query.FilterPredicate(Entity.VERSION_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN, 1L));
        List<Entity> all = getService().prepare(query).asList(FetchOptions.Builder.withDefaults());
        Assert.assertEquals(1, all.size());
        Entity e1 = all.get(0);
        Assert.assertNotSame(e2, e1);
        // cleanup
        getService().delete(e1.getKey(), e2.getKey());
    }


    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void tearDownDepA() throws Exception {
        tearDown();
    }

    @InSequence(1010)
    @Test
    @OperateOnDeployment("dep2")
    public void tearDownDepB() throws Exception {
        tearDown();
    }

    private DatastoreService getService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private void tearDown() {
        ((ExposedDatastoreService) getService()).clearCache();
    }

    private Entity createTestEntity(String kind, int id) {
        Key key = KeyFactory.createKey(kind, id);
        Entity entity = new Entity(key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    private void assertStoreContains(Entity entity) throws EntityNotFoundException {
        Entity lookup = getService().get(entity.getKey());
        Assert.assertNotNull(lookup);
        Assert.assertEquals(entity, lookup);
    }

    private void waitForSync() throws InterruptedException {
        sync(5000L);
    }

    protected void assertVersion(Entity entity, AtomicLong counter) throws Exception {
        entity.setProperty("counter", counter.get());
        Key key = getService().put(entity);
        entity = getService().get(key);

        Assert.assertEquals(counter.get(), entity.getProperty("counter"));

        long actualVersion = Entities.getVersionProperty(entity);
        Assert.assertEquals(counter.getAndIncrement(), actualVersion);
    }

}
