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
import com.google.appengine.api.quota.QuotaService;

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
     * Get quota service.
     *
     * @return quota service
     */
    QuotaService getQuotaService();

    /**
     * Get range.
     *
     * @param parent the parent
     * @param sequenceName the sequenceName
     * @param num the size of range
     * @return key range start
     */
    Long getRange(Key parent, String sequenceName, long num);

    /**
     * Check key range.
     *
     * @param keyRange the key range
     * @param sequenceName the sequence name
     * @return key range state
     */
    DatastoreService.KeyRangeState checkRange(KeyRange keyRange, String sequenceName);

    /**
     * Get transaction id.
     *
     * @return the tx id
     */
    String getTransactionId();
}
