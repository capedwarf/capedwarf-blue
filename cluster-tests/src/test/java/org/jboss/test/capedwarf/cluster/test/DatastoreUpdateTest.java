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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
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
public class DatastoreUpdateTest extends ClusteredTestBase {

    private DatastoreService service;

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void putStoresEntityOnDepA() throws Exception {
        Entity entity = createTestEntity("UPDATE", 1);
        entity.setProperty("prop1", "1_1");
        entity.setProperty("type", "NEW");

        getService().put(entity);
    }

    @InSequence(31)
    @Test
    @OperateOnDeployment("dep2")
    public void putStoresEntityOnDepB() throws Exception {
        Entity entity = createTestEntity("UPDATE", 2);
        entity.setProperty("prop1", "2_1");
        entity.setProperty("type", "NEW");

        getService().put(entity);
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep1")
    public void getAndUpdateEntityOnDepA() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("UPDATE", 1);
        Entity lookup = getService().get(key);
        Assert.assertNotNull(lookup);

        lookup.setProperty("prop1", "1_2");
        getService().put(lookup);
    }

    @InSequence(50)
    @Test
    @OperateOnDeployment("dep2")
    public void checkUpdatedEntityOnDepB() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("UPDATE", 1);
        Entity lookup = getService().get(key);
        Assert.assertNotNull(lookup);
        Assert.assertEquals("1_2", lookup.getProperty("prop1"));

        // update on B

        key = KeyFactory.createKey("UPDATE", 2);
        lookup = getService().get(key);
        Assert.assertNotNull(lookup);
        Assert.assertEquals("2_1", lookup.getProperty("prop1"));

        lookup.setProperty("prop1", "2_2");
        getService().put(lookup);
    }

    @InSequence(51)
    @Test
    @OperateOnDeployment("dep1")
    public void checkUpdatedEntityOnDepA() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("UPDATE", 2);
        Entity lookup = getService().get(key);
        Assert.assertNotNull(lookup);
        Assert.assertEquals("2_2", lookup.getProperty("prop1"));
    }

    @InSequence(72)
    @Test
    @OperateOnDeployment("dep1")
    public void queryOnA() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("UPDATE")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(73)
    @Test
    @OperateOnDeployment("dep2")
    public void queryOnB() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("UPDATE")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(80)
    @Test
    @OperateOnDeployment("dep1")
    public void updateTypeEntityOnDepA() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("UPDATE", 1);
        Entity lookup = getService().get(key);
        Assert.assertNotNull(lookup);

        lookup.setProperty("type", "DONE");
        getService().put(lookup);
    }

    @InSequence(81)
    @Test
    @OperateOnDeployment("dep2")
    public void updateTypeEntityOnDepB() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("UPDATE", 2);
        Entity lookup = getService().get(key);
        Assert.assertNotNull(lookup);

        lookup.setProperty("type", "DONE");
        getService().put(lookup);
    }


    @InSequence(92)
    @Test
    @OperateOnDeployment("dep1")
    public void queryTypeOnA() throws Exception {
        waitForSync();

        int count = getService()
                .prepare(new Query("UPDATE").setFilter(new Query.FilterPredicate("type", Query.FilterOperator.EQUAL, "DONE")))
                .countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(93)
    @Test
    @OperateOnDeployment("dep2")
    public void queryTypeOnB() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("UPDATE")
                .setFilter(new Query.FilterPredicate("type", Query.FilterOperator.EQUAL, "DONE")))
                .countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void tearDownDepA() throws Exception {
        getService().delete(KeyFactory.createKey("UPDATE", 1));
    }

    @InSequence(1010)
    @Test
    @OperateOnDeployment("dep2")
    public void tearDownDepB() throws Exception {
        getService().delete(KeyFactory.createKey("UPDATE", 2));
    }

    private DatastoreService getService() {
        if (service == null) {
            service = DatastoreServiceFactory.getDatastoreService();
        }
        return service;
    }

    private Entity createTestEntity(String kind, int id) {
        Key key = KeyFactory.createKey(kind, id);
        Entity entity = new Entity(key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    private void waitForSync() throws InterruptedException {
        sync(3000L);
    }

}
