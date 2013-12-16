/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.test.capedwarf.capabilities.test;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.capabilities.ExposedCapabilitiesService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class CapabilitiesServiceTest extends TestBase {

    private static final List<Capability> KNOWN_CAPABILITIES = Arrays.asList(Capability.BLOBSTORE,
            Capability.DATASTORE, Capability.DATASTORE_WRITE, Capability.IMAGES, Capability.MAIL, Capability.MEMCACHE,
            Capability.PROSPECTIVE_SEARCH, Capability.TASKQUEUE, Capability.URL_FETCH, Capability.XMPP);

    @Deployment
    public static Archive getDeployment() {
        return getCapedwarfDeployment();
    }

    @Test
    public void testServiceReturnsEnabledStatusAndNullScheduledDateForAllKnownCapabilities() {
        CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();
        for (Capability capability : KNOWN_CAPABILITIES) {
            CapabilityState status = service.getStatus(capability);
            assertEquals(capability, status.getCapability());
            assertNull(status.getScheduledDate());
            assertEquals(CapabilityStatus.ENABLED, status.getStatus());
        }
    }

    @Test
    public void testCapabilityStatusCanBeSet() {
        ExposedCapabilitiesService service = (ExposedCapabilitiesService) CapabilitiesServiceFactory.getCapabilitiesService();
        try {
            service.setCapabilityStatus(Capability.BLOBSTORE, CapabilityStatus.SCHEDULED_MAINTENANCE);
            CapabilityState state = service.getStatus(Capability.BLOBSTORE);

            assertEquals(CapabilityStatus.SCHEDULED_MAINTENANCE, state.getStatus());
        } finally {
            service.setCapabilityStatus(Capability.BLOBSTORE, CapabilityStatus.ENABLED);
        }
    }
}
