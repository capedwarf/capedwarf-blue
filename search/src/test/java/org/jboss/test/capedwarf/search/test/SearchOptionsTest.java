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

package org.jboss.test.capedwarf.search.test;

import java.util.Collections;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(All.class)
public class SearchOptionsTest extends SearchTestBase {

    @Test
    public void testLimit() {
        Index index = getTestIndex();
        createData(index);
        assertSearchYields(index, "", QueryOptions.newBuilder().setLimit(2).build(), 3, "fooaaa", "foobbb");
        assertSearchYields(index, "bar:bbb", QueryOptions.newBuilder().setLimit(1).build(), 2, "foobbb");
    }

    @Test
    public void testOffset() {
        Index index = getTestIndex();
        createData(index);
        assertSearchYields(index, "", QueryOptions.newBuilder().setOffset(1).build(), 3, "foobbb", "fooccc");
    }

    @Test
    public void testIdsOnly() {
        Index index = getTestIndex();
        createData(index);
        Results<ScoredDocument> results = getResults(index, "", QueryOptions.newBuilder().setReturningIdsOnly(true).build());
        for (ScoredDocument doc : results.getResults()) {
            assertTrue(doc.getFieldNames().isEmpty());
        }
    }

    @Test
    public void testFieldsToReturn() {
        Index index = getTestIndex();
        createData(index);
        Results<ScoredDocument> results = getResults(index, "bar:bbb", QueryOptions.newBuilder().setFieldsToReturn("baz").build());
        for (ScoredDocument doc : results.getResults()) {
            assertEquals(Collections.singleton("baz"), doc.getFieldNames());
            assertEquals(0, doc.getFieldCount("bar"));
            assertNull(doc.getFields("bar"));
        }
    }

    private void createData(Index index) {
        index.put(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.put(newDocument("foobbb", newField("bar").setText("bbb"), newField("baz").setText("bbb")));
        index.put(newDocument("fooccc", newField("bar").setText("bbb"), newField("baz").setText("bbb")));
    }



}
