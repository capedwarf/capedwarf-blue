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

import java.util.Arrays;

import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class GetIndexesWithNamespaceTestCase extends AbstractTest {

    @Test
    public void testListIndexesWithNamespaceIsIgnored() {
        Index fooAIndex = createIndexInNamespace("a", FOO_NAMESPACE);
        Index fooBIndex = createIndexInNamespace("b", FOO_NAMESPACE);
        createIndexInNamespace("a", BAR_NAMESPACE);
        createIndexInNamespace("b", BAR_NAMESPACE);

        SearchService fooSearchService = SearchServiceFactory.getSearchService(FOO_NAMESPACE);

        GetResponse<Index> response = fooSearchService.getIndexes(GetIndexesRequest.newBuilder());
        assertEquals(Arrays.asList(fooAIndex, fooBIndex), response.getResults());

        response = fooSearchService.getIndexes(GetIndexesRequest.newBuilder().setNamespace(FOO_NAMESPACE));
        assertEquals(Arrays.asList(fooAIndex, fooBIndex), response.getResults());

        response = fooSearchService.getIndexes(GetIndexesRequest.newBuilder().setNamespace(BAR_NAMESPACE));
        assertEquals(Arrays.asList(fooAIndex, fooBIndex), response.getResults());
    }

}
