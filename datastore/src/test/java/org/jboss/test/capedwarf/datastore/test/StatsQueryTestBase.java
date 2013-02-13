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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class StatsQueryTestBase extends TestBase {

    public static final String STAT_KIND = "__Stat_Kind__";
    public static final String STAT_TOTAL = "__Stat_Total__";
    public static final String STAT_NS_KIND = "__Stat_Ns_Kind__";
    public static final String STAT_NS_TOTAL = "__Stat_Ns_Total__";

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    protected static WebArchive getDefaultDeployment(boolean sync) {
        TestContext context = TestContext.asDefault();
        context.getProperties().put("enable.eager.datastore.stats", sync ? "sync" : "async");
        return getCapedwarfDeployment(context).addClass(StatsQueryTestBase.class);
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

    protected List<Entity> getStatsList(String statsKind) {
        return getStatsList(statsKind, null);
    }

    protected List<Entity> getStatsList(String statsKind, Query.Filter filter) {
        Query query = new Query(statsKind).addSort("timestamp", Query.SortDirection.DESCENDING);
        if (filter != null) {
            query.setFilter(filter);
        }
        return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    }

    protected Entity getStatsEntity(String statsKind) {
        return getStatsEntity(statsKind, null);
    }

    protected Entity getStatsEntity(String statsKind, Query.Filter filter) {
        List<Entity> list = getStatsList(statsKind, filter);
        if (list.isEmpty()) {
            Entity stats = new Entity(statsKind);
            stats.setProperty("count", 0L);
            stats.setProperty("bytes", 0L);
            return stats;
        } else {
            return list.get(0);
        }
    }

    protected abstract void doSync();

    @Test
    public void testTotalStats() throws Exception {
        Key k1 = null;
        Key k2 = null;
        try {
            Entity initialStats = currentTotalStats();

            NamespaceManager.set("namespace1");
            Entity initialNs1Stats = currentNsTotalStats();

            NamespaceManager.set("namespace2");
            Entity initialNs2Stats = currentNsTotalStats();

            Entity e2 = new Entity("SC");
            k2 = datastore.put(e2);
            doSync();
            assertStatsLargerBy(e2, initialNs2Stats, currentNsTotalStats());
            assertStatsLargerBy(e2, initialStats, currentTotalStats());

            NamespaceManager.set("namespace1");
            assertStatsEqual(initialNs1Stats, currentNsTotalStats());

            Entity e1 = new Entity("SC");
            k1 = datastore.put(e1);
            doSync();
            assertStatsLargerBy(e1, initialNs1Stats, currentNsTotalStats());
            assertStatsLargerBy(Arrays.asList(e1, e2), initialStats, currentTotalStats());

            NamespaceManager.set("namespace2");
            assertStatsLargerBy(e2, initialNs2Stats, currentNsTotalStats());

            datastore.delete(k2);
            doSync();
            assertStatsEqual(initialNs2Stats, currentNsTotalStats());
            assertStatsLargerBy(e1, initialStats, currentTotalStats());

            NamespaceManager.set("namespace1");
            datastore.delete(k1);
            doSync();
            assertStatsEqual(initialNs1Stats, currentNsTotalStats());
            assertStatsEqual(initialStats, currentTotalStats());
        } finally {
            cleanup(k1, k2);
            doSync();
        }
    }

    @Test
    public void testKindStats() throws Exception {
        Entity e1 = new Entity("SCK");
        e1.setProperty("x", "original");
        Key k1 = datastore.put(e1);

        doSync();

        Entity e3 = new Entity("QWE");
        e3.setProperty("foo", "bar");
        Key k3 = datastore.put(e3);

        doSync();

        Query.FilterPredicate filter = new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL, "SCK");

        Entity kindStats = getStatsEntity(STAT_KIND, filter);
        long count = getCount(kindStats);
        assertEquals(1L, count);

        Entity e2 = new Entity("SCK");
        e2.setProperty("y", "replacement");
        Key k2 = datastore.put(e2);

        doSync();

        kindStats = getStatsEntity(STAT_KIND, filter);
        assertEquals(count + 1, getCount(kindStats));

        assertEquals(1, countEntitiesMatching(getStatsList(STAT_KIND), "kind_name", "QWE"));

        datastore.delete(k2);

        doSync();

        kindStats = getStatsEntity(STAT_KIND, filter);
        assertEquals(count, getCount(kindStats));

        datastore.delete(k1, k3);
    }


    private void cleanup(Key... keys) {
        String ns = NamespaceManager.get();
        for (Key key : keys) {
            if (key != null) {
                NamespaceManager.set(key.getNamespace());
                datastore.delete(key);
            }
        }
        NamespaceManager.set(ns);
    }

    private Entity currentTotalStats() {
        String ns = NamespaceManager.get();
        if (ns == null || !ns.equals("")) {
            NamespaceManager.set("");
        }
        try {
            return getStatsEntity(STAT_TOTAL);
        } finally {
            if (ns == null || !ns.equals("")) {
                NamespaceManager.set(ns);
            }
        }
    }

    private Entity currentNsTotalStats() {
        return getStatsEntity(STAT_NS_TOTAL);
    }


    private void assertStatsEqual(Entity expected, Entity actual) {
        assertEquals(getCount(expected), getCount(actual));
        assertEquals(getBytes(expected), getBytes(actual));
    }

    private void assertStatsLargerBy(Entity entity, Entity previousStats, Entity newStats) {
        assertStatsLargerBy(Collections.singleton(entity), previousStats, newStats);
    }

    private void assertStatsLargerBy(Collection<Entity> entities, Entity previousStats, Entity newStats) {
        assertEquals("count", getCount(previousStats) + entities.size(), getCount(newStats));
        assertEquals("bytes", getBytes(previousStats) + countBytes(entities), getBytes(newStats));
    }

    private long getBytes(Entity allStats) {
        return (Long) allStats.getProperty("bytes");
    }

    private long getCount(Entity allStats) {
        return (Long) allStats.getProperty("count");
    }

    private int countBytes(Collection<Entity> entities) {
        int bytes = 0;
        for (Entity entity : entities) {
            bytes += countBytes(entity);
        }
        return bytes;
    }

    private int countEntitiesMatching(List<Entity> stats, String propertyName, String propertyValue) {
        int count = 0;
        for (Entity stat : stats) {
            String kindName = stat.getProperty(propertyName).toString();
            if (propertyValue.equals(kindName)) count++;
        }
        return count;
    }
}
