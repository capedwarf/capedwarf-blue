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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListIndexesRequest;
import com.google.appengine.api.search.ListIndexesResponse;
import com.google.appengine.api.search.SearchService;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
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
        this.cache = InfinispanUtils.<CacheKey, CacheValue>getCache(CacheName.SEARCH).getAdvancedCache().with(classLoader);
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
        ListIndexesMapper mapper = new ListIndexesMapper(request, resolveNamespace());
        ListIndexesReducer reducer = new ListIndexesReducer();
        ListIndexesCollator collator = new ListIndexesCollator(request, cache);

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


}
