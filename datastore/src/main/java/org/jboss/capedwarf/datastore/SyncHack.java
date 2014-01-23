/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.util.Set;
import java.util.logging.Logger;

import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.SetKey;
import org.jboss.capedwarf.shared.components.Slot;

/**
 * Do we make DatastoreService force sync ops on cache for certain callers.
 * e.g. MapReduce lib needs synced ops in order to work
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class SyncHack {
    private static final Logger log = Logger.getLogger(SyncHack.class.getName());

    static boolean forceSync() {
        Set<String> callers = ComponentRegistry.getInstance().getComponent(new SetKey<String>(Slot.SYNC_HACK));
        if (callers == null || callers.isEmpty())
            return false;

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : elements) {
            String clazz = ste.getClassName();
            if (callers.contains(clazz)) {
                log.finest("Matched " + clazz + ", forcing sync ops.");
                return true;
            }
        }
        return false;
    }
}
