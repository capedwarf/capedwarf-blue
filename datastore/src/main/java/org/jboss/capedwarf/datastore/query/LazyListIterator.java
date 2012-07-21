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

import java.util.ListIterator;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyListIterator<E> extends LazyIterator<E> implements ListIterator<E> {
    private final int index;
    private ListIterator<E> delegate;

    public LazyListIterator(LazyList<E> lazyList) {
        this(lazyList, 0);
    }

    public LazyListIterator(LazyList<E> lazyList, int index) {
        super(lazyList);
        this.index = index;
    }

    protected ListIterator<E> getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = lazyList.getDelegate().listIterator(index);
                }
            }
        }
        return delegate;
    }

    public boolean hasPrevious() {
        check();
        return getDelegate().hasPrevious();
    }

    public E previous() {
        check();
        return getDelegate().previous();
    }

    public int nextIndex() {
        check();
        return getDelegate().nextIndex();
    }

    public int previousIndex() {
        check();
        return getDelegate().previousIndex();
    }

    public void set(E e) {
        check();
        getDelegate().set(e);
    }

    public void add(E e) {
        check();
        getDelegate().add(e);
    }
}
