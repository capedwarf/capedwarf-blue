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
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class PostLoadIterator<E> implements Iterator<E> {
    private Iterator<E> delegate;
    private int fetchSize;
    private QueryHolder holder;
    private int current;
    private List<E> buffer = new ArrayList<E>();

    PostLoadIterator(Iterator<E> delegate, int fetchSize, QueryHolder holder) {
        this.delegate = delegate;
        this.fetchSize = fetchSize;
        this.holder = holder;
    }

    private void check() {
        int size = buffer.size();
        if (current == size) {
            for (int i = 0; i < fetchSize && delegate.hasNext(); i++) {
                E result = delegate.next();
                holder.executePostLoad(result);
                buffer.add(result);
            }
        }
    }

    public synchronized boolean hasNext() {
        check();
        return (current < buffer.size());
    }

    public synchronized E next() {
        if (!hasNext()) {
            throw new NoSuchElementException("current: " + current + "; buffer.size: " + buffer.size());
        }
        return buffer.get(current++);
    }

    public synchronized void remove() {
        check();
        buffer.remove(--current);
    }
}
