/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultIterator;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class QueryResultIteratorImpl<E> implements QueryResultIterator<E> {

    private Iterator<E> delegate;

    public QueryResultIteratorImpl(Iterator<E> iterator) {
        this.delegate = iterator;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public E next() {
        return delegate.next();
    }

    public void remove() {
        delegate.remove();
    }

    public Cursor getCursor() {
        return null;  // TODO
    }

    public List<Index> getIndexList() {
        return null;  // TODO -- null is OK, as we don't know; as per spec
    }
}
