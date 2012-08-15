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

package org.jboss.test.capedwarf.search;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.ListIndexesRequest;
import com.google.appengine.api.search.ListIndexesResponse;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class ListIndexesWithStartIndexNameTestCase extends AbstractTest {

    @Test
    public void testListIndexesWithStartIndexName() {
        Index aIndex = createIndex("a");
        Index bIndex = createIndex("b");
        Index cIndex = createIndex("c");

        ListIndexesResponse response = service.listIndexes(ListIndexesRequest.newBuilder().setStartIndexName("b").build());
        assertEquals(Arrays.asList(bIndex, cIndex), response.getIndexes());

        response = service.listIndexes(ListIndexesRequest.newBuilder().setStartIndexName("b").setIncludeStartIndex(true).build());
        assertEquals(Arrays.asList(bIndex, cIndex), response.getIndexes());

        response = service.listIndexes(ListIndexesRequest.newBuilder().setStartIndexName("b").setIncludeStartIndex(false).build());
        assertEquals(Arrays.asList(cIndex), response.getIndexes());
    }

}
