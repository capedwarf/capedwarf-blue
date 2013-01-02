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

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class PostLoadList<E> implements List<E> {
    private List<E> delegate;
    private int fetchSize;
    private QueryHolder holder;
    private volatile int index = -1;

    PostLoadList(List<E> delegate, int fetchSize, QueryHolder holder) {
        this.delegate = delegate;
        this.fetchSize = fetchSize;
        this.holder = holder;
    }

    public E get(int i) {
        // get the result, so the delegate checks if index is ok
        final E result = delegate.get(i);
        if (i > index) {
            synchronized (this) {
                if (i > index) {
                    int n = index;
                    while (n < i) n += fetchSize;
                    int size = delegate.size();
                    for (int j = index + 1; j <= n && j < size; j++) {
                        holder.executePostLoad(delegate.get(j));
                    }
                    index = n;
                }
            }
        }
        return result;
    }

    public int size() {
        int size = delegate.size();
        if (size > 0) {
            get(size - 1);
        }
        return size;
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int current;

            public synchronized boolean hasNext() {
                boolean result = (current < delegate.size());
                if (result) {
                    get(current);
                }
                return result;
            }

            public synchronized E next() {
                return get(current++);
            }

            public synchronized void remove() {
                PostLoadList.this.remove(--current);
            }
        };
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            private int current = index;
            private int previous = index - 1;

            public boolean hasNext() {
                boolean result = (current < delegate.size());
                if (result) {
                    get(current);
                }
                return result;
            }

            public E next() {
                previous = current;
                return get(current++);
            }

            public boolean hasPrevious() {
                return (current > 0);
            }

            public E previous() {
                previous = current;
                return get(--current);
            }

            public int nextIndex() {
                return current;
            }

            public int previousIndex() {
                return current - 1;
            }

            public void remove() {
                if (previous > current) --previous;
                PostLoadList.this.remove(--current);
            }

            public void set(E e) {
                delegate.set(previous, e);
            }

            public void add(E e) {
                delegate.add(current, e);
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        List<E> sub = delegate.subList(fromIndex, toIndex);
        get(toIndex - 1);
        PostLoadList<E> post = new PostLoadList<E>(sub, fetchSize, holder);
        post.index = sub.size() - 1;
        return post;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        size();
        return delegate.contains(o);
    }

    public Object[] toArray() {
        size();
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        size();
        //noinspection SuspiciousToArrayCall
        return delegate.toArray(a);
    }

    public boolean add(E e) {
        return delegate.add(e);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        size();
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return delegate.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return delegate.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        size();
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        size();
        return delegate.retainAll(c);
    }

    public void clear() {
        delegate.clear();
    }

    public E set(int index, E element) {
        size();
        return delegate.set(index, element);
    }

    public void add(int index, E element) {
        size();
        delegate.add(index, element);
    }

    public E remove(int index) {
        size();
        return delegate.remove(index);
    }

    public int indexOf(Object o) {
        size();
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        size();
        return delegate.lastIndexOf(o);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
