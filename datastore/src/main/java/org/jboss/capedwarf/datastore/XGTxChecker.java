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

package org.jboss.capedwarf.datastore;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Key;

/**
 * XG checker.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class XGTxChecker implements TxChecker {
    private static final int MAX_ENTITY_GROUPS = 5;

    // 1 per tx, we should be fine wrt sync
    private Set<Key> roots = new HashSet<Key>();

    public boolean isInvalid(Key currentRoot, Key key) {
        TxTracker.track(currentRoot);

        roots.add(currentRoot);

        return (roots.size() > MAX_ENTITY_GROUPS);
    }

    public void clear() {
        // if it was added into roots, it passed used check
        // so we can safely remove it
        for (Key root : roots) {
            TxTracker.remove(root);
        }
    }
}
