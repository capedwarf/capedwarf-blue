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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListIndexesRequest;
import com.google.appengine.api.search.ListIndexesResponse;
import com.google.appengine.api.search.SearchService;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.*;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.ConfigurationCallbacks;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfSearchService implements SearchService {

    private String namespace;

    private Cache<CacheKey, CacheValue> cache;

    public CapedwarfSearchService() {
        initCache();
    }

    public CapedwarfSearchService(String namespace) {
        NamespaceManager.validateNamespace(namespace);
        this.namespace = namespace;
        initCache();
    }

    private void initCache() {
        ClassLoader classLoader = Application.getAppClassloader();
        this.cache = InfinispanUtils.<CacheKey, CacheValue>getCache(CacheName.SEARCH, ConfigurationCallbacks.SEARCH_CALLBACK).getAdvancedCache().with(classLoader);
    }

    public Index getIndex(IndexSpec indexSpec) {
        return new CapedwarfSearchIndex(indexSpec.getName(), resolveNamespace(), indexSpec.getConsistency(), cache);
    }

    public Index getIndex(IndexSpec.Builder builder) {
        return getIndex(builder.build());
    }

    public String getNamespace() {
        return namespace;
    }

    private String resolveNamespace() {
        if (namespace == null) {
            String namespace = NamespaceManager.get();
            return namespace == null ? "" : namespace;
        } else {
            return namespace;
        }
    }

    public ListIndexesResponse listIndexes(ListIndexesRequest request) {
        ListIndexesMapper mapper = new ListIndexesMapper(request);
        ListIndexesReducer reducer = new ListIndexesReducer();
        ListIndexesCollator collator = new ListIndexesCollator(request);

        MapReduceTask<CacheKey, CacheValue, FullIndexSpec, Void> task = new MapReduceTask<CacheKey, CacheValue, FullIndexSpec, Void>(cache);
        return task.mappedWith(mapper).reducedWith(reducer).execute(collator);
    }

    public Future<ListIndexesResponse> listIndexesAsync(final ListIndexesRequest request) {
        return ExecutorFactory.wrap(new Callable<ListIndexesResponse>() {
            public ListIndexesResponse call() throws Exception {
                return listIndexes(request);
            }
        });
    }

    public void clear() {
        cache.clear();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }


    class ListIndexesMapper implements Mapper<CacheKey, CacheValue, FullIndexSpec, Void> {

        private final ListIndexesRequest request;

        public ListIndexesMapper(ListIndexesRequest request) {
            this.request = request;
        }

        public void map(CacheKey key, CacheValue value, Collector<FullIndexSpec, Void> collector) {
            if (startIndexNameMatches(key) && indexNamePrefixMatches(key) && namespaceMatches(key)) {
                FullIndexSpec fullIndexSpec = new FullIndexSpec(key.getNamespace(), key.getIndexName(), Consistency.GLOBAL);
                collector.emit(fullIndexSpec, null);
            }
        }

        private boolean startIndexNameMatches(CacheKey key) {
            String startIndexName = request.getStartIndexName();
            if (startIndexName == null) {
                return true;
            }
            String indexName = key.getIndexName();
            if (request.isIncludeStartIndex()) {
                return startIndexName.compareTo(indexName) <= 0;
            } else {
                return startIndexName.compareTo(indexName) < 0;
            }
        }

        private boolean indexNamePrefixMatches(CacheKey key) {
            return request.getIndexNamePrefix() == null || key.getIndexName().startsWith(request.getIndexNamePrefix());
        }

        private boolean namespaceMatches(CacheKey key) {
            return key.getNamespace().equals(resolveNamespace());
//            return request.getNamespace() == null || key.getNamespace().equals(request.getNamespace());
        }
    }

    class ListIndexesReducer implements Reducer<FullIndexSpec, Void> {
        public Void reduce(FullIndexSpec reducedKey, Iterator<Void> iter) {
            return null;
        }
    }

    class ListIndexesCollator implements Collator<FullIndexSpec, Void, ListIndexesResponse> {

        private final ListIndexesRequest request;

        public ListIndexesCollator(ListIndexesRequest request) {
            this.request = request;
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
}
