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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultIterator;
import org.infinispan.query.CacheQuery;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyQueryResultIterator<E> extends LazyChecker implements QueryResultIterator<E> {
    private volatile QueryResultIterator<E> delegate;

    public LazyQueryResultIterator(QueryHolder holder, FetchOptions fetchOptions) {
        super(holder, fetchOptions);
    }

    @SuppressWarnings("unchecked")
    protected QueryResultIterator<E> getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    apply();
                    Iterator iterator = createQueryIterator();
                    iterator = new QueryResultProcessor(holder.getQuery()).process(iterator);
                    delegate = new QueryResultIteratorImpl<E>(iterator);
                }
            }
        }
        return delegate;
    }

    private Iterator createQueryIterator() {
        final CacheQuery cacheQuery = holder.getCacheQuery();
        final Integer chunkSize = fetchOptions.getChunkSize();
        if (chunkSize == null) {
            return cacheQuery.iterator();
        } else {
            return cacheQuery.iterator(chunkSize);
        }
    }

    public List<Index> getIndexList() {
        check();
        return getDelegate().getIndexList();
    }

    public Cursor getCursor() {
        check();
        return getDelegate().getCursor();
    }

    public boolean hasNext() {
        check();
        return getDelegate().hasNext();
    }

    public E next() {
        check();
        return getDelegate().next();
    }

    public void remove() {
        check();
        getDelegate().remove();
    }
}
