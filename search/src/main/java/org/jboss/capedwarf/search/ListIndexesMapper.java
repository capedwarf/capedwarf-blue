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

import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.ListIndexesRequest;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class ListIndexesMapper implements Mapper<CacheKey, CacheValue, FullIndexSpec, String> {

    private String namespace;
    private String indexNamePrefix;
    private String startIndexName;
    private boolean includeStartIndex;

    public ListIndexesMapper() {

    }

    public ListIndexesMapper(ListIndexesRequest request, String namespace) {
        this.namespace = namespace;
        indexNamePrefix = request.getIndexNamePrefix();
        startIndexName = request.getStartIndexName();
        includeStartIndex = startIndexName != null && request.isIncludeStartIndex();
    }

    public void map(CacheKey key, CacheValue value, Collector<FullIndexSpec, String> collector) {
        if (startIndexNameMatches(key) && indexNamePrefixMatches(key) && namespaceMatches(key)) {
            FullIndexSpec fullIndexSpec = new FullIndexSpec(key.getNamespace(), key.getIndexName(), Consistency.GLOBAL);
            collector.emit(fullIndexSpec, "");
        }
    }

    private boolean startIndexNameMatches(CacheKey key) {
        if (startIndexName == null) {
            return true;
        }
        if (includeStartIndex) {
            return startIndexName.compareTo(key.getIndexName()) <= 0;
        } else {
            return startIndexName.compareTo(key.getIndexName()) < 0;
        }
    }

    private boolean indexNamePrefixMatches(CacheKey key) {
        return indexNamePrefix == null || key.getIndexName().startsWith(indexNamePrefix);
    }

    private boolean namespaceMatches(CacheKey key) {
        return key.getNamespace().equals(namespace);
//            return request.getNamespace() == null || key.getNamespace().equals(request.getNamespace());
    }
}
