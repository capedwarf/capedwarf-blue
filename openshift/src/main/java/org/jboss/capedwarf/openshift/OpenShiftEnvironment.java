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

package org.jboss.capedwarf.openshift;

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.quota.QuotaService;
import org.jboss.capedwarf.environment.Environment;
import org.kohsuke.MetaInfServices;

/**
 * OpenShift environment.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class OpenShiftEnvironment implements Environment {
    public String getPartition() {
        return null; // TODO
    }

    public String getDomain() {
        return null; // TODO
    }

    public CapabilityState getState(Capability capability) {
        return null; // TODO
    }

    @Override
    public void setState(Capability capability, CapabilityStatus status) {
        throw new UnsupportedOperationException();
    }

    public QuotaService getQuotaService() {
        return null;  // TODO
    }

    public Long getRange(String appId, Key parent, String sequenceName, long num) {
        return null;  // TODO
    }

    public DatastoreService.KeyRangeState checkRange(String appId, KeyRange keyRange, String sequenceName) {
        return null;  // TODO
    }

    public void updateRange(String appId, long id, String sequenceName, long allocationSize) {
        // TODO
    }
}
