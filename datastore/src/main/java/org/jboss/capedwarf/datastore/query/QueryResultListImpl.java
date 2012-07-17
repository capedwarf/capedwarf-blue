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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.QueryResultList;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class QueryResultListImpl<E> extends ArrayList<E> implements QueryResultList<E> {
    private AtomicInteger current = new AtomicInteger();

    public QueryResultListImpl(Collection<? extends E> c) {
        super(c);
    }

    public synchronized Cursor getCursor() {
        final Cursor cursor = JBossCursorHelper.createCursor(current);
        current = new AtomicInteger(current.get());
        return cursor;
    }

    public List<Index> getIndexList() {
        return null;  // TODO -- null is OK, as we don't know; as per spec
    }

    @Override
    public E get(int index) {
        current.set(index + 1);
        return super.get(index);
    }

    @Override
    public void add(int index, E element) {
        current.set(index);
        super.add(index, element);
    }

    @Override
    public E remove(int index) {
        current.set(index);
        return super.remove(index);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> delegate = super.iterator();
        return new Iterator<E>() {
            public boolean hasNext() {
                return delegate.hasNext();
            }

            public E next() {
                current.incrementAndGet();
                return delegate.next();
            }

            public void remove() {
                current.decrementAndGet();
                delegate.remove();
            }
        };
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        current.set(index);
        final ListIterator<E> delegate = super.listIterator(index);
        return new ListIterator<E>() {
            public boolean hasNext() {
                return delegate.hasNext();
            }

            public E next() {
                current.incrementAndGet();
                return delegate.next();
            }

            public boolean hasPrevious() {
                return delegate.hasPrevious();
            }

            public E previous() {
                current.decrementAndGet();
                return delegate.previous();
            }

            public int nextIndex() {
                current.incrementAndGet();
                return delegate.nextIndex();
            }

            public int previousIndex() {
                current.decrementAndGet();
                return delegate.previousIndex();
            }

            public void remove() {
                current.decrementAndGet();
                delegate.remove();
            }

            public void set(E e) {
                delegate.set(e);
            }

            public void add(E e) {
                delegate.add(e);
            }
        };
    }
}
