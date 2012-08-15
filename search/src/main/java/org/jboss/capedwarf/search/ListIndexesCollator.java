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

package org.jboss.capedwarf.search;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.ListIndexesRequest;
import com.google.appengine.api.search.ListIndexesResponse;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class ListIndexesCollator implements Collator<FullIndexSpec, Void, ListIndexesResponse> {

    private final ListIndexesRequest request;
    private final Cache<CacheKey,CacheValue> cache;

    public ListIndexesCollator(ListIndexesRequest request, Cache<CacheKey, CacheValue> cache) {
        this.request = request;
        this.cache = cache;
    }

    public ListIndexesResponse collate(Map<FullIndexSpec, Void> reducedResults) {

        List<FullIndexSpec> list = new ArrayList<FullIndexSpec>(reducedResults.keySet());
        Collections.sort(list);

        if (request.getOffset() != null) {
            list = request.getOffset() < list.size() ? list.subList(request.getOffset(), list.size()) : Collections.<FullIndexSpec>emptyList();
        }
        if (request.getLimit() != null) {
            list = list.subList(0, Math.min(request.getLimit(), list.size()));
        }

        List<Index> indexes = new ArrayList<Index>();
        for (FullIndexSpec fullIndexSpec : list) {
            CapedwarfSearchIndex index = new CapedwarfSearchIndex(
                fullIndexSpec.getName(),
                fullIndexSpec.getNamespace(),
                fullIndexSpec.getConsistency(),
                cache);
            indexes.add(index);
        }

        return new ListIndexesResponse(indexes) {
        };
    }
}
