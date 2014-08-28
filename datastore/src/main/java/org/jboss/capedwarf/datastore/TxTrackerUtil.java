/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import com.google.appengine.api.datastore.Key;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * Track tx usage wrt entity groups.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class TxTrackerUtil {
    private static volatile TxTracker tracker;

    private static TxTracker getTracker() {
        if (tracker == null) {
            synchronized (TxTrackerUtil.class) {
                if (tracker == null) {
                    if (Compatibility.getInstance().isEnabled(Compatibility.Feature.FORCE_STANDALONE_TX_TRACKER)) {
                        tracker = new StandaloneTxTracker();
                    } else {
                        tracker = new ClusteredTxTracker();
                    }
                }
            }
        }
        return tracker;
    }

    static void track(Key currentRoot) {
        getTracker().track(currentRoot);
    }

    static void beforeCompletion(Key currentRoot) {
        getTracker().beforeCompletion(currentRoot);
    }

    static void afterCompletion(int status, Key currentRoot) {
        getTracker().afterCompletion(status, currentRoot);
    }

    static void dump() {
        getTracker().dump();
    }
}
