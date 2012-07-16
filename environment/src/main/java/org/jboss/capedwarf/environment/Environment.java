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

package org.jboss.capedwarf.environment;

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

/**
 * Environment info.
 *
 * e.g. on OpenShift cloud, etc
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Environment {
    /**
     * Get domain name.
     *
     * @return domain name
     */
    String getDomain();

    /**
     * Get capability state.
     *
     * @param capability the capability in question
     * @return capability's state
     */
    CapabilityState getState(Capability capability);

    /**
     * Get unique id for the kind.
     *
     * @param kind the kind
     * @param allocationSize the allocation size
     * @return unique id
     */
    Long getUniqueId(String kind, int allocationSize);

    /**
     * Get range.
     *
     * @param parent the parent
     * @param kind the kind
     * @param num the size of range
     * @return new key range
     */
    KeyRange getRange(Key parent, String kind, long num);

    /**
     * Check key range.
     *
     * @param keyRange the key range
     * @return key range state
     */
    DatastoreService.KeyRangeState checkRange(KeyRange keyRange);

    /**
     * Get transaction id.
     *
     * @return the tx id
     */
    String getTransactionId();
}
