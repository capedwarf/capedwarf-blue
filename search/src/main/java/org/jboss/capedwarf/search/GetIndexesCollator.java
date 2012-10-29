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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.Collator;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
* @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
*/
class GetIndexesCollator implements Collator<FullIndexSpec, String, GetResponse<Index>> {

    private final GetIndexesRequest request;
    private final Cache<CacheKey,CacheValue> cache;

    public GetIndexesCollator(GetIndexesRequest request, Cache<CacheKey, CacheValue> cache) {
        this.request = request;
        this.cache = cache;
    }

    public GetResponse<Index> collate(Map<FullIndexSpec, String> reducedResults) {

        List<FullIndexSpec> list = new ArrayList<FullIndexSpec>(reducedResults.keySet());
        Collections.sort(list);

        if (request.getOffset() != null) {
            list = request.getOffset() < list.size() ? list.subList(request.getOffset(), list.size()) : Collections.<FullIndexSpec>emptyList();
        }
        if (request.getLimit() != null) {
            list = list.subList(0, Math.min(request.getLimit(), list.size()));
        }

        List<Index> indexes = new ArrayList<Index>();
        for (FullIndexSpec fis : list) {
            CapedwarfSearchIndex index = new CapedwarfSearchIndex(fis.getName(), fis.getNamespace(), cache);
            indexes.add(index);
        }

        return new GetResponse<Index>(indexes){};
    }
}
