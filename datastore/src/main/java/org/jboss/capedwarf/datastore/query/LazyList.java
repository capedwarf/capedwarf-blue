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

import com.google.appengine.api.datastore.FetchOptions;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyList<E> extends LazyChecker implements List<E> {
    private List<E> delegate;

    LazyList(QueryHolder holder, FetchOptions fetchOptions) {
        super(holder, fetchOptions);
    }

    private LazyList(QueryHolder holder, FetchOptions fetchOptions, List<E> delegate) {
        super(holder, fetchOptions);
        this.delegate = delegate;
    }

    protected List<E> getDelegate() {
        return delegate;
    }

    public int size() {
        check();
        return getDelegate().size();
    }

    public boolean isEmpty() {
        check();
        return getDelegate().isEmpty();
    }

    public boolean contains(Object o) {
        check();
        return getDelegate().contains(o);
    }

    public Iterator<E> iterator() {
        return new LazyIterator<E>(this);
    }

    public Object[] toArray() {
        check();
        return getDelegate().toArray();
    }

    public <T> T[] toArray(T[] a) {
        check();
        return getDelegate().toArray(a);
    }

    public boolean add(E e) {
        check();
        return getDelegate().add(e);
    }

    public boolean remove(Object o) {
        check();
        return getDelegate().remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        check();
        return getDelegate().containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        check();
        return getDelegate().addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        check();
        return getDelegate().addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        check();
        return getDelegate().removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        check();
        return getDelegate().retainAll(c);
    }

    public void clear() {
        check();
        getDelegate().clear();
    }

    public E get(int index) {
        check();
        return getDelegate().get(index);
    }

    public E set(int index, E element) {
        check();
        return getDelegate().set(index, element);
    }

    public void add(int index, E element) {
        check();
        getDelegate().add(index, element);
    }

    public E remove(int index) {
        check();
        return getDelegate().remove(index);
    }

    public int indexOf(Object o) {
        check();
        return getDelegate().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        check();
        return getDelegate().lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return new LazyListIterator<E>(this);
    }

    public ListIterator<E> listIterator(int index) {
        return new LazyListIterator<E>(this, index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        List<E> list = getDelegate().subList(fromIndex, toIndex);
        return new LazyList<E>(holder, fetchOptions, list);
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getDelegate().equals(obj);
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }
}
