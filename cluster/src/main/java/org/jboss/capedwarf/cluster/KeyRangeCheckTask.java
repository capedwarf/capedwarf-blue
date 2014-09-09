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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.infinispan.NewTxTask;

/**
 * Key range check task.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class KeyRangeCheckTask extends NewTxTask<String, Long, DatastoreService.KeyRangeState> {
    private final KeyRange keyRange;
    private final String sequenceName;

    public KeyRangeCheckTask(KeyRange keyRange, String sequenceName) {
        this.keyRange = keyRange;
        this.sequenceName = sequenceName;
    }

    protected DatastoreService.KeyRangeState callInTx() throws Exception {
        final AdvancedCache<String, Long> ac = getCache().getAdvancedCache();
        final String cacheKey = sequenceName;
        
        if (ac.lock(cacheKey) == false)
            throw new IllegalArgumentException("Cannot get a lock on id generator for " + cacheKey);

        Long nextId = ac.get(cacheKey);
        if (nextId == null)
            nextId = 1L;

        final Key start = keyRange.getStart();
        // Empty is unsupported here
        return start.getId() < nextId ? DatastoreService.KeyRangeState.COLLISION : DatastoreService.KeyRangeState.CONTENTION;
    }
}
