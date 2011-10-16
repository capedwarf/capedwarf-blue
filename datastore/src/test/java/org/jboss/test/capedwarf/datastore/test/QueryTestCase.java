/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.test.capedwarf.datastore.test;

import com.google.appengine.api.datastore.*;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.*;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.appengine.api.datastore.Query.FilterOperator.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public abstract class QueryTestCase {

    protected static final String TEST_ENTITY_KIND = "test";
    protected static final String SINGLE_PROPERTY_NAME = "prop";

    protected DatastoreService service;

    private int idSequence;

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(QueryTestCase.class)
                .addAsManifestResource("jboss/jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
    }

    protected static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month + 1, day);
        return cal.getTime();
    }

    @Before
    public void setUp() {
        service = DatastoreServiceFactory.getDatastoreService();
    }

    @After
    public void tearDown() {
        service.delete((Iterable) null);  // clears cache completely
    }

    protected void assertSingleResult(Entity expectedEntity, Query query) {
        PreparedQuery preparedQuery = service.prepare(query);
        assertEquals("number of results", 1, preparedQuery.countEntities());

        Entity entityFromQuery = preparedQuery.asSingleEntity();
        assertEquals(expectedEntity, entityFromQuery);
    }

    protected void assertNoResults(Query query) {
        PreparedQuery preparedQuery = service.prepare(query);
        Assert.assertEquals("number of results", 0, preparedQuery.countEntities());
    }

    protected TestEntityBuilder createTestEntity() {
        return createEntity(TEST_ENTITY_KIND, ++idSequence);
    }

    protected TestEntityBuilder createEntity(String kind, int id) {
        return new TestEntityBuilder(kind, id);
    }

    protected Entity storeTestEntityWithSingleProperty(Object value) {
        return createTestEntity()
                .withProperty(SINGLE_PROPERTY_NAME, value)
                .store();
    }

    protected Query createQuery(Query.FilterOperator operator, Object value) {
        return createQuery()
                .addFilter(SINGLE_PROPERTY_NAME, operator, value);
    }

    protected Query createQuery() {
        return new Query(TEST_ENTITY_KIND);
    }

    protected Matcher<Set<Entity>> queryReturns(Entity... entities) {
        return new IsEqual<Set<Entity>>(new HashSet<Entity>(Arrays.asList(entities)));
    }

    protected Set<Entity> whenFilteringBy(Query.FilterOperator operator, Object value) {
        Query query = createQuery(operator, value);
        List<Entity> results = service.prepare(query).asList(withDefaults());
        return new HashSet<Entity>(results);
    }

    /**
     * Tests if querying by GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN and LESS_THAN_OR_EQUAL returns
     * the correct results.
     *
     * @param lowValue  The lowest value among the three values.
     * @param midValue  The medium value among the three values.
     * @param highValue The highest value among the three values.
     */
    protected void testInequalityQueries(Object lowValue, Object midValue, Object highValue) {
        // we don't store the entities in a nice order (low, then mid, then high), because that could make the test
        // pass if the order in which the entities were stored was used for comparing.
        Entity highEntity = storeTestEntityWithSingleProperty(highValue);
        Entity lowEntity = storeTestEntityWithSingleProperty(lowValue);
        Entity midEntity = storeTestEntityWithSingleProperty(midValue);

        assertThat(whenFilteringBy(GREATER_THAN, lowValue), queryReturns(midEntity, highEntity));
        assertThat(whenFilteringBy(GREATER_THAN_OR_EQUAL, midValue), queryReturns(midEntity, highEntity));
        assertThat(whenFilteringBy(LESS_THAN, highValue), queryReturns(midEntity, lowEntity));
        assertThat(whenFilteringBy(LESS_THAN_OR_EQUAL, midValue), queryReturns(midEntity, lowEntity));

        service.delete(lowEntity.getKey());
        service.delete(midEntity.getKey());
        service.delete(highEntity.getKey());
    }

    /**
     * Tests whether given two entities with each having a single property, whose value is either foo or bar; when
     * querying by EQUAL foo, the query returns foo; and when querying by NOT_EQUAL foo, the query returns bar.
     *
     * @param foo property value for first entity
     * @param bar property value for second entity
     */
    protected void testEqualityQueries(Object foo, Object bar) {
        Entity fooEntity = storeTestEntityWithSingleProperty(foo);
        Entity barEntity = storeTestEntityWithSingleProperty(bar);

        assertThat(whenFilteringBy(EQUAL, foo), queryReturns(fooEntity));
        assertThat(whenFilteringBy(NOT_EQUAL, foo), queryReturns(barEntity));

        service.delete(fooEntity.getKey());
        service.delete(barEntity.getKey());
    }

    protected class TestEntityBuilder {

        private Entity entity;

        public TestEntityBuilder(String kind, int id) {
            entity = new Entity(kind, id);
        }

        public TestEntityBuilder withProperty(String key, Object value) {
            entity.setProperty(key, value);
            return this;
        }

        public Entity store() {
            service.put(entity);
            return entity;
        }
    }

}
