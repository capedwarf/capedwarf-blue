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

package org.jboss.capedwarf.datastore.notifications;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.SetMultimap;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.infinispan.BaseTxTask;
import org.jboss.capedwarf.datastore.NamespaceServiceInternal;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractNamespaceTask extends BaseTxTask<String, SetMultimap<String, String>, Void> {
    static final String NAMESPACES = NamespaceServiceInternal.NAMESPACES;

    protected final Entity trigger;

    public AbstractNamespaceTask(Entity trigger) {
        this.trigger = trigger;
    }

    protected Void callInTx() throws Exception {
        final AdvancedCache<String, SetMultimap<String, String>> ac = getCache().getAdvancedCache();

        if (ac.lock(NAMESPACES) == false)
            throw new IllegalArgumentException("Cannot get a lock on key for " + NAMESPACES);

        SetMultimap<String, String> namespaces = getCache().get(NAMESPACES);
        applyTrigger(namespaces);
        if (namespaces.isEmpty() == false) {
            getCache().put(NAMESPACES, namespaces);
        }
        return null;
    }

    protected abstract void applyTrigger(SetMultimap<String, String> namespaces);
}