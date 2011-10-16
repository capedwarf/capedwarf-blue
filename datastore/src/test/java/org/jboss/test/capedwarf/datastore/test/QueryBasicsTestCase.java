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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static org.junit.Assert.assertEquals;

/**
 * Datastore querying basic tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryBasicsTestCase extends QueryTestCase {

    @Test
    public void queryingByKindOnlyReturnsEntitiesOfRequestedKind() throws Exception {
        Entity person = new Entity(KeyFactory.createKey("Person", 1));
        service.put(person);

        Entity address = new Entity(KeyFactory.createKey("Address", 1));
        service.put(address);

        assertSingleResult(person, new Query("Person"));
    }

    @Test(expected = PreparedQuery.TooManyResultsException.class)
    public void singleEntityThrowsTooManyResultsExceptionWhenMoreThanOneResult() throws Exception {
        createEntity("Person", 1).store();
        createEntity("Person", 2).store();

        PreparedQuery preparedQuery = service.prepare(new Query("Person"));
        preparedQuery.asSingleEntity();
    }

    @Test
    public void testMultipleFilters() throws Exception {
        Entity johnDoe = createEntity("Person", 1)
                .withProperty("name", "John")
                .withProperty("lastName", "Doe")
                .store();

        Entity johnBooks = createEntity("Person", 2)
                .withProperty("name", "John")
                .withProperty("lastName", "Books")
                .store();

        Entity janeDoe = createEntity("Person", 3)
                .withProperty("name", "Jane")
                .withProperty("lastName", "Doe")
                .store();

        Query query = new Query("Person")
                .addFilter("name", EQUAL, "John")
                .addFilter("lastName", EQUAL, "Doe");

        assertSingleResult(johnDoe, query);
    }

    @Test
    public void testKeysOnly() throws Exception {
        Entity john = createEntity("Person", 1)
                .withProperty("name", "John")
                .store();

        Query query = new Query("Person").setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);

        Entity entity = preparedQuery.asSingleEntity();
        assertEquals(john.getKey(), entity.getKey());
    }

}
