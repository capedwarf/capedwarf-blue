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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ForwardingIterator;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.FetchOptions;
import org.infinispan.query.ResultIterator;
import org.jboss.capedwarf.datastore.EntityUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EntityLoader {

    private final Query query;
    private final CacheQuery cacheQuery;

    public EntityLoader(Query query, CacheQuery cacheQuery) {
        this.query = query;
        this.cacheQuery = cacheQuery;
    }

    public List<Object> getList() {
        boolean conversionNeeded = mustConvertResultsToEntities();
        List<Object> results = cacheQuery.list();
        List<Object> list = new ArrayList<Object>(results.size());
        for (Object result : results) {
            if (conversionNeeded) {
                list.add(Projections.convertToEntity(query, result));
            } else {
                list.add(EntityUtils.cloneEntity((Entity) result));
            }
        }
        return list;
    }

    public Iterator<Object> getIterator(Integer chunkSize) {
        final Iterator<Object> iterator;
        if (chunkSize == null) {
            iterator = toClosingIterator(cacheQuery.iterator());
        } else if (chunkSize == Integer.MAX_VALUE) {
            iterator = cacheQuery.list().iterator();
        } else {
            iterator = toClosingIterator(cacheQuery.iterator(new FetchOptions().fetchSize(chunkSize)));
        }
        if (mustConvertResultsToEntities()) {
            return new WrappingIterator(iterator);
        } else {
            return new CloningIterator(iterator);
        }
    }

    private Iterator<Object> toClosingIterator(ResultIterator iterator) {
        return new ClosingIterator(iterator);
    }

    private boolean mustConvertResultsToEntities() {
        return query.isKeysOnly() || !query.getProjections().isEmpty();
    }

    private class WrappingIterator implements Iterator<Object> {
        private final Iterator iterator;

        public WrappingIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            return Projections.convertToEntity(query, iterator.next());
        }

        public void remove() {
            iterator.remove();
        }
    }

    private class CloningIterator extends ForwardingIterator<Object> {
        private final Iterator<Object> iterator;

        public CloningIterator(Iterator<Object> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected Iterator<Object> delegate() {
            return iterator;
        }

        public Object next() {
            Entity entity = (Entity) iterator.next();
            return EntityUtils.cloneEntity(entity);
        }
    }

    private static class ClosingIterator extends ForwardingIterator<Object> {
        private final ResultIterator delegate;

        private ClosingIterator(ResultIterator delegate) {
            this.delegate = delegate;
        }

        protected Iterator<Object> delegate() {
            return delegate;
        }

        protected void finalize() throws Throwable {
            try {
                delegate.close(); // any better way to do this?
            } catch (Throwable t) {
                System.err.println(t);
            } finally {
                super.finalize();
            }
        }
    }
}
