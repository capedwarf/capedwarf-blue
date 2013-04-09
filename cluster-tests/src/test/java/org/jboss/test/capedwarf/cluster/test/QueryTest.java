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
        Entity entity = new Entity("QT", 1);
        getService().put(entity);
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void deleteInB() throws Exception {
        Entity entity = getService().get(KeyFactory.createKey("QT", 1));
        Assert.assertNotNull(entity);
        getService().delete(entity.getKey());
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
        Entity entity = new Entity("QT", 2);
        getService().put(entity);
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    @InSequence(50)
    @Test
    @OperateOnDeployment("dep1")
    public void deleteAndQueryInA() throws Exception {
        Entity entity = getService().get(KeyFactory.createKey("QT", 2));
        Assert.assertNotNull(entity);
        getService().delete(entity.getKey());

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

    private DatastoreService getService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private void tearDown() {
        ((ExposedDatastoreService) getService()).clearCache();
    }
}
