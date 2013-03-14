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

package org.jboss.capedwarf.datastore.query;

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import org.infinispan.query.CacheQuery;
import org.jboss.capedwarf.datastore.PostLoadHandle;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

/**
 * JBoss GAE PreparedQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PreparedQueryImpl extends QueryHolder implements PreparedQuery {
    private final PostLoadHandle callback;
    private final Query gaeQuery;
    private final CacheQuery cacheQuery;
    private final boolean inTx;

    public PreparedQueryImpl(PostLoadHandle callback, Query gaeQuery, CacheQuery cacheQuery, boolean inTx) {
        this.callback = callback;
        this.gaeQuery = gaeQuery;
        this.cacheQuery = cacheQuery;
        this.inTx = inTx;
    }

    Query getQuery() {
        return gaeQuery;
    }

    CacheQuery getCacheQuery() {
        return cacheQuery;
    }

    boolean isInTx() {
        return inTx;
    }

    boolean isDistinct() {
        return getQuery().getDistinct();
    }

    void executePostLoad(Object result) {
        if (result instanceof Entity) {
            callback.execute((Entity) result);
        }
    }

    public List<Entity> asList(FetchOptions fetchOptions) {
        return asQueryResultList(fetchOptions);
    }

    @SuppressWarnings("unchecked")
    public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
        return new LazyQueryResultList<Entity>(this, fetchOptions);
    }

    public Iterable<Entity> asIterable() {
        return asIterable(withDefaults());
    }

    public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
        return asQueryResultIterable(fetchOptions);
    }

    public QueryResultIterable<Entity> asQueryResultIterable() {
        return asQueryResultIterable(withDefaults());
    }

    public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
        return new LazyQueryResultIterable<Entity>(this, fetchOptions);
    }

    public Iterator<Entity> asIterator() {
        return asIterator(withDefaults());
    }

    public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
        return asQueryResultIterator(fetchOptions);
    }

    public QueryResultIterator<Entity> asQueryResultIterator() {
        return asQueryResultIterator(withDefaults());
    }

    @SuppressWarnings("unchecked")
    public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
        return new LazyQueryResultIterator<Entity>(this, fetchOptions);
    }

    public Entity asSingleEntity() throws TooManyResultsException {
        Iterator<Entity> iterator = asIterator();
        Entity firstResult = iterator.hasNext() ? iterator.next() : null;
        if (iterator.hasNext()) {
            throw new TooManyResultsException();
        }
        return firstResult;
    }

    public int countEntities() {
        return countEntities(withDefaults());
    }

    public int countEntities(FetchOptions fetchOptions) {
        if (isDistinct()) {
            return asQueryResultList(fetchOptions).size();
        } else {
            return new CountEntities(this, fetchOptions).count();
        }
    }

    private static class CountEntities extends LazyChecker {
        private CountEntities(QueryHolder holder, FetchOptions fetchOptions) {
            super(holder, fetchOptions);
        }

        private int count() {
            check();
            apply();

            int totalResults = holder.getCacheQuery().getResultSize();
            Integer offset = fetchOptions.getOffset();
            Integer limit = fetchOptions.getLimit();
            if (offset == null) {
                offset = 0;
            }

            if (offset > totalResults) {
                return 0;
            } else {
                if (limit == null || offset + limit > totalResults) {
                    return totalResults - offset;
                } else {
                    return limit;
                }
            }
        }
    }
}
