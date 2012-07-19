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


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.QueryResultList;
import org.jboss.capedwarf.datastore.LazyChecker;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyQueryResultList<E> extends LazyChecker implements QueryResultList<E> {
    private final QueryResultList<E> delegate;

    LazyQueryResultList(QueryResultList<E> delegate, Key ancestor, boolean inTx) {
        super(ancestor, inTx);
        this.delegate = delegate;
    }

    public List<Index> getIndexList() {
        check();
        return delegate.getIndexList();
    }

    public Cursor getCursor() {
        check();
        return delegate.getCursor();
    }

    public int size() {
        check();
        return delegate.size();
    }

    public boolean isEmpty() {
        check();
        return delegate.isEmpty();
    }

    public boolean contains(Object o) {
        check();
        return delegate.contains(o);
    }

    public Iterator<E> iterator() {
        Iterator<E> iterator = delegate.iterator();
        return new LazyIterator<E>(iterator, ancestor, inTx);
    }

    public Object[] toArray() {
        check();
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        check();
        return delegate.toArray(a);
    }

    public boolean add(E e) {
        check();
        return delegate.add(e);
    }

    public boolean remove(Object o) {
        check();
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        check();
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        check();
        return delegate.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        check();
        return delegate.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        check();
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        check();
        return delegate.retainAll(c);
    }

    public void clear() {
        check();
        delegate.clear();
    }

    public E get(int index) {
        check();
        return delegate.get(index);
    }

    public E set(int index, E element) {
        check();
        return delegate.set(index, element);
    }

    public void add(int index, E element) {
        check();
        delegate.add(index, element);
    }

    public E remove(int index) {
        check();
        return delegate.remove(index);
    }

    public int indexOf(Object o) {
        check();
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        check();
        return delegate.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        ListIterator<E> iterator = delegate.listIterator();
        return new LazyListIterator<E>(iterator, ancestor, inTx);
    }

    public ListIterator<E> listIterator(int index) {
        ListIterator<E> iterator = delegate.listIterator(index);
        return new LazyListIterator<E>(iterator, ancestor, inTx);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        check();
        return delegate.subList(fromIndex, toIndex);
    }
}
