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

package org.jboss.test.capedwarf.cluster.test;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Stats query.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class StatsQueryTest extends TestBase {

    protected static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        context.getProperties().put("enable.eager.datastore.stats", "sync");
        return getCapedwarfDeployment(context);
    }

    @Deployment(name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    protected static List<Entity> getStatsList(String statsKind) {
        return getStatsList(statsKind, null);
    }

    protected static List<Entity> getStatsList(String statsKind, Query.Filter filter) {
        DatastoreService service = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(statsKind).addSort("timestamp", Query.SortDirection.DESCENDING);
        if (filter != null) {
            query.setFilter(filter);
        }
        return service.prepare(query).asList(FetchOptions.Builder.withDefaults());
    }

    protected static Entity getStatsEntity(String statsKind) {
        return getStatsEntity(statsKind, null);
    }

    protected static Entity getStatsEntity(String statsKind, Query.Filter filter) {
        List<Entity> list = getStatsList(statsKind, filter);
        Assert.assertFalse(list.isEmpty());
        return list.get(0);
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(10)
    public void testPutInA() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity("CSQ", 1);
        entity.setProperty("node", "a");
        ds.put(entity);

        sync();
    }

    @Test @OperateOnDeployment("dep2") @InSequence(20)
    public void testPutInB() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity("CSQ", 2);
        entity.setProperty("node", "b");
        ds.put(entity);

        sync();
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(30)
    public void countOnA() throws Exception {
        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "CSQ");
        Entity kindStat = getStatsEntity("__Stat_Kind__", filter);
        Assert.assertEquals(2L, kindStat.getProperty("count"));
    }

    @Test
    @OperateOnDeployment("dep2") @InSequence(40)
    public void countOnB() throws Exception {
        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "CSQ");
        Entity kindStat = getStatsEntity("__Stat_Kind__", filter);
        Assert.assertEquals(2L, kindStat.getProperty("count"));
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(50)
    public void deleteOnA() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.delete(KeyFactory.createKey("CSQ", 2));

        sync();
    }

    @Test
    @OperateOnDeployment("dep2") @InSequence(60)
    public void recountOnB() throws Exception {
        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "CSQ");
        Entity kindStat = getStatsEntity("__Stat_Kind__", filter);
        Assert.assertEquals(1L, kindStat.getProperty("count"));
    }

    @Test
    @OperateOnDeployment("dep2") @InSequence(70)
    public void deleteOnB() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.delete(KeyFactory.createKey("CSQ", 1));

        sync();
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(80)
    public void recountOnA() throws Exception {
        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "CSQ");
        Entity kindStat = getStatsEntity("__Stat_Kind__", filter);
        Assert.assertEquals(0L, kindStat.getProperty("count"));
    }
}
