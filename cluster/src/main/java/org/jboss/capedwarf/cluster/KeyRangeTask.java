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

package org.jboss.capedwarf.cluster;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.infinispan.BaseTxTask;

/**
 * Key range task.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class KeyRangeTask extends BaseTxTask<String, Long, KeyRange> {
    private final Key parent;
    private final String kind;
    private final long num;

    public KeyRangeTask(Key parent, String kind, long num) {
        this.parent = parent;
        this.kind = kind;
        this.num = num;
    }

    protected KeyRange callInTx() throws Exception {
        final AdvancedCache<String, Long> ac = getCache().getAdvancedCache();

        if (ac.lock(kind) == false)
            throw new IllegalArgumentException("Cannot get a lock on id generator for " + kind);

        Long nextId = ac.get(kind);
        if (nextId == null)
            nextId = 1L;

        ac.put(kind, nextId + num);

        return new KeyRange(parent, kind, nextId, nextId + num - 1);
    }
}
