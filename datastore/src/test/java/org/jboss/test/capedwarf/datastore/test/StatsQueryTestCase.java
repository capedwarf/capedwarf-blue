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

package org.jboss.test.capedwarf.datastore.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class StatsQueryTestCase extends BaseTest {
    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        context.getProperties().put("enable.eager.datastore.stats", "sync");
        return getCapedwarfDeployment(context);
    }

    protected static long countBytes(Entity entity) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(entity);
            out.flush();
            return baos.size();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot count entity: " + entity);
        }
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
    public void testTotalStats() throws Exception {
        DatastoreService service = DatastoreServiceFactory.getDatastoreService();

        Entity e1 = new Entity("SC");
        e1.setProperty("x", "original");
        Key k1 = service.put(e1);

        Entity allStats = getStatsEntity("__Stat_Total__");
        long count = (Long) allStats.getProperty("count");
        long bytes = (Long) allStats.getProperty("bytes");

        Entity e2 = new Entity("SC");
        e2.setProperty("y", "replacement");
        Key k2 = service.put(e2);

        allStats = getStatsEntity("__Stat_Total__");
        long count2 = (Long) allStats.getProperty("count");
        Assert.assertEquals(count + 1, count2);
        long bytes2 = (Long) allStats.getProperty("bytes");
        long cb = countBytes(e2);
        Assert.assertEquals(bytes + cb, bytes2);

        service.delete(k2);

        allStats = getStatsEntity("__Stat_Total__");
        long count3 = (Long) allStats.getProperty("count");
        Assert.assertEquals(count, count3);
        long bytes3 = (Long) allStats.getProperty("bytes");
        Assert.assertEquals(bytes, bytes3);

        service.delete(k1);
    }

    @Test
    public void testKindStats() throws Exception {
        DatastoreService service = DatastoreServiceFactory.getDatastoreService();

        Entity e1 = new Entity("SCK");
        e1.setProperty("x", "original");
        Key k1 = service.put(e1);

        Entity e3 = new Entity("QWE");
        e3.setProperty("foo", "bar");
        Key k3 = service.put(e3);

        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "SCK");

        Entity kindStats = getStatsEntity("__Stat_Kind__", filter);
        long count = (Long) kindStats.getProperty("count");
        Assert.assertEquals(1L, count);

        Entity e2 = new Entity("SCK");
        e2.setProperty("y", "replacement");
        Key k2 = service.put(e2);

        kindStats = getStatsEntity("__Stat_Kind__", filter);
        long count2 = (Long) kindStats.getProperty("count");
        Assert.assertEquals(count + 1, count2);

        int qwes = 0;
        for (Entity stat : getStatsList("__Stat_Kind__")) {
            String kindName = stat.getProperty("kind_name").toString();
            if ("QWE".equals(kindName)) qwes++;
        }
        Assert.assertEquals(1, qwes);

        service.delete(k2);

        kindStats = getStatsEntity("__Stat_Kind__", filter);
        long count3 = (Long) kindStats.getProperty("count");
        Assert.assertEquals(count, count3);

        service.delete(k1, k3);
    }
}
