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

import java.util.UUID;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.environment.AbstractEnvironment;
import org.jboss.capedwarf.environment.Environment;
import org.kohsuke.MetaInfServices;

/**
 * Cluster env.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(Environment.class)
public class ClusterEnvironment extends AbstractEnvironment {
    public String getPartition() {
        return MASTER_SERVER_PARTITION;
    }

    public String getDomain() {
        return "cluster-mode"; // TODO - per node?
    }

    public Long getRange(String appId, Key parent, String sequenceName, long num) {
        return InfinispanUtils.submit(appId, CacheName.DIST, new KeyGeneratorTask(sequenceName, num), sequenceName);
    }

    public DatastoreService.KeyRangeState checkRange(String appId, KeyRange keyRange, String sequenceName) {
        return InfinispanUtils.submit(appId, CacheName.DIST, new KeyRangeCheckTask(keyRange, sequenceName), sequenceName);
    }

    public void updateRange(String appId, long id, String sequenceName, long allocationSize) {
        InfinispanUtils.submit(appId, CacheName.DIST, new KeyRangeUpdateTask(id, sequenceName, allocationSize), sequenceName);
    }

    public String getTransactionId() {
        return UUID.randomUUID().toString();
    }
}
