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

import java.util.concurrent.atomic.AtomicInteger;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import org.infinispan.query.CacheQuery;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;

/**
 * JBoss Cursor helper.
 * We bytecode hacked cursor, hence we need a bunch of reflection to use it.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class JBossCursorHelper {
    private static TargetInvocation<Integer> getIndex = ReflectionUtils.cacheInvocation(Cursor.class, "getIndex");

    static Cursor createCursor(final AtomicInteger index) {
        return ReflectionUtils.newInstance(Cursor.class, new Class[]{AtomicInteger.class}, new Object[]{index});
    }

    static Cursor createListCursor(FetchOptions fetchOptions) {
        if (fetchOptions == null)
            return null;

        final Cursor end = fetchOptions.getEndCursor();
        if (end != null)
            return end;

        final Integer limit = fetchOptions.getLimit();
        if (limit != null) {
            int offset = 0;
            final Cursor start = fetchOptions.getStartCursor();
            if (start != null) {
                offset = readIndex(start);
            } else {
                final Integer x = fetchOptions.getOffset();
                if (x != null) {
                    offset = x;
                }
            }
            return createCursor(new AtomicInteger(offset + limit));
        } else {
            return null; // cannot determine cursor
        }
    }

    static void applyStartCursor(Cursor start, CacheQuery cacheQuery) {
        final int index = readIndex(start);
        cacheQuery.firstResult(index);
    }

    static void applyEndCursor(Cursor end, CacheQuery cacheQuery, Cursor start) {
        final int last = readIndex(end);
        final int first = (start != null) ? readIndex(start) : 0;
        cacheQuery.maxResults(last - first + 1);
    }

    private static int readIndex(Cursor cursor) {
        try {
            return getIndex.invoke(cursor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}