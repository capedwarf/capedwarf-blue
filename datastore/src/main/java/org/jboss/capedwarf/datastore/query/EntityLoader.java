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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.QueryIterator;

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
        List<Object> results = cacheQuery.list();
        if (specialLoadingNeeded()) {
            List<Object> list = new ArrayList<Object>(results.size());
            for (Object result : results) {
                list.add(convertToEntity(result));
            }
            return list;
        } else {
            //noinspection unchecked
            return (List)results;
        }
    }

    public Iterator<Object> getIterator(Integer chunkSize) {
        QueryIterator iterator = chunkSize == null ? cacheQuery.iterator() : cacheQuery.iterator(chunkSize);
        if (specialLoadingNeeded()) {
            return new WrappingIterator(iterator);
        } else {
            return iterator;
        }
    }

    private boolean specialLoadingNeeded() {
        return query.isKeysOnly() || !query.getProjections().isEmpty();
    }

    private Entity convertToEntity(Object result) {
        if (result instanceof Entity) {
            return Entity.class.cast(result);
        } else {
            Object[] row = (Object[]) result;
            Entity entity = new Entity((Key) row[0]);
            int i = 1;
            for (Projection projection : query.getProjections()) {
                if (projection instanceof PropertyProjection) {
                    PropertyProjection propertyProjection = (PropertyProjection) projection;
                    entity.setProperty(propertyProjection.getName(), row[i]);
                } else {
                    throw new IllegalStateException("Unsupported projection type: " + projection.getClass());
                }
                i++;
            }
            return entity;
        }
    }

    private class WrappingIterator implements Iterator<Object> {
        private final QueryIterator iterator;

        public WrappingIterator(QueryIterator iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            return convertToEntity(iterator.next());
        }

        public void remove() {
            iterator.remove();
        }
    }
}
