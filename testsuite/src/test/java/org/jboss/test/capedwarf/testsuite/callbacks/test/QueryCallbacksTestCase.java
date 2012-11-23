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

package org.jboss.test.capedwarf.testsuite.callbacks.test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(All.class)
public class QueryCallbacksTestCase extends AbstractQueryCallbacksTest {

    @Test
    public void testListGet() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());
        list.get(0);
        assertCallbackInvokedFully();
    }

    @Test
    public void testListGetWithChunk() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withChunkSize(2));
        list.get(0);
        assertPostLoadCallbackInvokedTimes(2);
        list.get(1);
        assertNoCallbackInvoked();
        list.get(2);
        assertPostLoadCallbackInvokedTimes(2);
    }

    @Test
    public void testListRemove() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());
        list.remove(0);
        assertCallbackInvokedFully();
    }

    @Test
    public void testListToArray() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());
        list.toArray();
        assertCallbackInvokedFully();
    }

    @Test
    public void testListSize() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());
        list.size();
        assertCallbackInvokedFully();
    }

    @Test
    public void testListSizeWithChunk() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withChunkSize(2));
        list.size();
        assertCallbackInvokedFully();
    }

    @Test
    public void testListRemoveAll() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());

        Entity e1 = new Entity(KIND);
        e1.setProperty("y", "a");
        list.removeAll(Collections.singleton(e1));
        assertCallbackInvokedFully();

        Entity e2 = new Entity(KIND);
        e2.setProperty("x", 1);
        list.removeAll(Collections.singleton(e2));
        assertNoCallbackInvoked();
    }

    @Test
    public void testListIterator() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withDefaults());
        ListIterator<Entity> iterator = list.listIterator();
        iterator.next();
        assertCallbackInvokedFully();
    }

    @Test
    public void testListIteratorWithChunk() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withChunkSize(1));
        ListIterator<Entity> iterator = list.listIterator();
        iterator.next();
        assertPostLoadCallbackInvokedTimes(1);
        iterator.next();
        assertPostLoadCallbackInvokedTimes(1);
        iterator.previous();
        assertNoCallbackInvoked();
    }

    @Test
    public void testListIteratorWithChunkTwo() throws Exception {
        List<Entity> list = asList(FetchOptions.Builder.withChunkSize(2));
        ListIterator<Entity> iterator = list.listIterator();
        iterator.hasNext();
        assertPostLoadCallbackInvokedTimes(2);
        iterator.next();
        assertNoCallbackInvoked();
        iterator.hasNext();
        assertNoCallbackInvoked();
        iterator.next();
        assertNoCallbackInvoked();
        iterator.previous();
        assertNoCallbackInvoked();
        iterator.next();
        assertNoCallbackInvoked();
        iterator.hasNext();
        assertPostLoadCallbackInvokedTimes(2);
        iterator.next();
        assertNoCallbackInvoked();
    }

    @Test
    public void testIterators() throws Exception {
        Iterator<Entity> iterator = asIterator(FetchOptions.Builder.withDefaults());
        iterator.next();
        assertCallbackInvokedFully();
    }

    @Test
    public void testIteratorsWithChunk() throws Exception {
        Iterator<Entity> iterator = asIterator(FetchOptions.Builder.withChunkSize(1));
        iterator.next();
        assertPostLoadCallbackInvokedTimes(1);
        iterator.next();
        assertPostLoadCallbackInvokedTimes(1);
        iterator.next();
        assertPostLoadCallbackInvokedTimes(1);
    }

    @Test
    public void testIteratorsWithChunkTwo() throws Exception {
        Iterator<Entity> iterator = asIterator(FetchOptions.Builder.withChunkSize(2));
        iterator.next();
        assertPostLoadCallbackInvokedTimes(2);
        iterator.next();
        assertNoCallbackInvoked();
        iterator.next();
        assertPostLoadCallbackInvokedTimes(2);
    }

}
