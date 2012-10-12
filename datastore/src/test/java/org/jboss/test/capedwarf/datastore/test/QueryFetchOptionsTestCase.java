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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import static java.util.Arrays.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryFetchOptionsTestCase extends QueryTest {

    private Entity foo1;
    private Entity foo2;
    private Entity foo3;
    private Entity foo4;
    private Entity foo5;

    @Before
    public void setUp() {
        super.setUp();
        foo1 = createEntity("Foo", 1).withProperty("bar", "a").store();
        foo2 = createEntity("Foo", 2).withProperty("bar", "b").store();
        foo3 = createEntity("Foo", 3).withProperty("bar", "c").store();
        foo4 = createEntity("Foo", 4).withProperty("bar", "d").store();
        foo5 = createEntity("Foo", 5).withProperty("bar", "e").store();
    }

    @Test
    public void testDefaults() {
        List<Entity> results = executeQuery(withDefaults());
        assertEquals(asList(foo1, foo2, foo3, foo4, foo5), results);
    }

    @Test
    public void testLimit() {
        List<Entity> results = executeQuery(withLimit(2));
        assertEquals(asList(foo1, foo2), results);
    }

    @Test
    public void testOffset() {
        List<Entity> results = executeQuery(withOffset(3));
        assertEquals(asList(foo4, foo5), results);
    }

    @Test
    public void testOffsetAndLimit() {
        List<Entity> results = executeQuery(withOffset(1).limit(1));
        assertEquals(asList(foo2), results);
    }

    @Test
    public void testStartCursor() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withStartCursor(cursor));
        assertEquals(asList(foo4, foo5), results);
    }

    @Test
    public void testStartCursorAndLimit() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withStartCursor(cursor).limit(1));
        assertEquals(asList(foo4), results);
    }

    @Test
    public void testStartCursorAndOffset() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withStartCursor(cursor).offset(1));
        assertEquals(asList(foo5), results);
    }

    @Test
    public void testEndCursor() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withEndCursor(cursor));
        assertEquals(asList(foo1, foo2, foo3), results);
    }

    @Test
    public void testEndCursorAndOffset() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withEndCursor(cursor).offset(1));
        assertEquals(asList(foo2, foo3), results);
    }

    @Test
    public void testEndCursorAndLimit() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withEndCursor(cursor).limit(2));
        assertEquals(asList(foo1, foo2), results);

        results = executeQuery(withEndCursor(cursor).limit(5)); // even if limit is past endCursor, endCursor will still apply
        assertEquals(asList(foo1, foo2, foo3), results);
    }

    @Test
    public void testEndCursorAndOffsetAndLimit() {
        QueryResultList<Entity> results = executeQuery(withLimit(3));
        Cursor cursor = results.getCursor();    // points to foo4

        results = executeQuery(withEndCursor(cursor).offset(1).limit(2));
        assertEquals(asList(foo2, foo3), results);

        results = executeQuery(withEndCursor(cursor).offset(1).limit(5));
        assertEquals(asList(foo2, foo3), results);
    }

    private QueryResultList<Entity> executeQuery(FetchOptions fetchOptions) {
        Query query = new Query("Foo").addSort("bar");
        return service.prepare(query).asQueryResultList(fetchOptions);
    }



}
