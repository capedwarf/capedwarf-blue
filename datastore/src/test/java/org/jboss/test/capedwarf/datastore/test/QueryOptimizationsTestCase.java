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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.RawValue;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Datastore querying optimizations tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class QueryOptimizationsTestCase extends QueryTest {

    @Test
    public void testKeysOnly() throws Exception {
        Entity john = createEntity("Person", 1)
            .withProperty("name", "John")
            .withProperty("surname", "Doe")
            .store();

        Query query = new Query("Person").setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);

        Entity entity = preparedQuery.asSingleEntity();
        assertEquals(john.getKey(), entity.getKey());
        assertNull(entity.getProperty("name"));
        assertNull(entity.getProperty("surname"));
    }

    @Test
    public void testProjections() throws Exception {
        Entity e = createEntity("Product", 1)
                .withProperty("price", 123L)
                .withProperty("percent", 0.123)
                .withProperty("x", -0.321)
                .withProperty("diff", -5L)
                .withProperty("weight", 10L)
                .store();

        Query query = new Query("Product")
                .addProjection(new PropertyProjection("price", Long.class))
                .addProjection(new PropertyProjection("percent", Double.class))
                .addProjection(new PropertyProjection("x", Double.class))
                .addProjection(new PropertyProjection("diff", Long.class));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());
        assertEquals(e.getProperty("price"), result.getProperty("price"));
        assertEquals(e.getProperty("percent"), result.getProperty("percent"));
        assertEquals(e.getProperty("x"), result.getProperty("x"));
        assertEquals(e.getProperty("diff"), result.getProperty("diff"));
        assertNull(result.getProperty("weight"));
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingProjectedProperty() throws Exception {
        Entity e1 = createEntity("Kind", 1)
            .withProperty("foo", "foo")
            .store();

        Entity e2 = createEntity("Kind", 2)
            .withProperty("bar", "bar")
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("foo", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingAllProjectedProperties() throws Exception {
        Entity e1 = createEntity("Kind", 1)
            .withProperty("foo", "foo")
            .withProperty("bar", "bar")
            .store();

        Entity e2 = createEntity("Kind", 2)
            .withProperty("foo", "foo")
            .store();

        Entity e3 = createEntity("Kind", 3)
            .withProperty("bar", "bar")
            .store();

        Entity e4 = createEntity("Kind", 4)
            .withProperty("baz", "baz")
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("foo", String.class))
            .addProjection(new PropertyProjection("bar", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionsWithoutType() throws Exception {
        Entity e = createEntity("Product", 1)
                .withProperty("long", 123L)
                .store();

        Query query = new Query("Product")
                .addProjection(new PropertyProjection("long", null));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());

        RawValue rawValue = (RawValue) result.getProperty("long");
        assertEquals(Long.valueOf(123L), rawValue.asType(Long.class));
        assertEquals(Long.valueOf(123L), rawValue.asStrictType(Long.class));
    }

    @Ignore("CAPEDWARF-67")
    @Test
    public void testProjectionOfCollectionProperties() throws Exception {
        Entity e = createEntity("Kind", 1)
            .withProperty("prop", Arrays.asList("bbb", "ccc", "aaa"))
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("prop", String.class));

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(withDefaults());
        System.out.println("results.get(0) = " + results.get(0));
        assertEquals(3, results.size());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);
        Entity thirdResult = results.get(2);

        assertEquals(e.getKey(), firstResult.getKey());
        assertEquals(e.getKey(), secondResult.getKey());
        assertEquals(e.getKey(), thirdResult.getKey());
        assertEquals("aaa", firstResult.getProperty("prop"));
        assertEquals("bbb", secondResult.getProperty("prop"));
        assertEquals("ccc", thirdResult.getProperty("prop"));
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingProjections() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingKeysOnly() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .store();

        Query query = new Query("Product")
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testEntityOnlyContainsProjectedProperties() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        Entity firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(1, firstResult.getProperties().size());
        assertEquals("price", firstResult.getProperties().keySet().iterator().next());

        query = new Query("Product")
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(0, firstResult.getProperties().size());
    }

    private void assertResultsInOrder(Query query, Entity a, Entity b) {
        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);

        assertEquals(b.getKey(), firstResult.getKey());
        assertEquals(a.getKey(), secondResult.getKey());
    }
}
