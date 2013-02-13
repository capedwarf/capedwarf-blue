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

import java.util.Arrays;

import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(All.class)
public class GetIndexesWithStartIndexNameTest extends SearchTestBase {

    @Test
    public void testListIndexesWithStartIndexName() {
        createIndex("a");
        Index bIndex = createIndex("b");
        Index cIndex = createIndex("c");

        GetResponse<Index> response = service.getIndexes(GetIndexesRequest.newBuilder().setStartIndexName("b"));
        assertEquals(Arrays.asList(bIndex, cIndex), response.getResults());

        response = service.getIndexes(GetIndexesRequest.newBuilder().setStartIndexName("b").setIncludeStartIndex(true));
        assertEquals(Arrays.asList(bIndex, cIndex), response.getResults());

        response = service.getIndexes(GetIndexesRequest.newBuilder().setStartIndexName("b").setIncludeStartIndex(false));
        assertEquals(Arrays.asList(cIndex), response.getResults());
    }

}
