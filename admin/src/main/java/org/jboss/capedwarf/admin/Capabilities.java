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

package org.jboss.capedwarf.admin;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import org.jboss.capedwarf.capabilities.ExposedCapabilitiesService;

import static com.google.appengine.api.capabilities.Capability.*;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named
@RequestScoped
public class Capabilities {

    private static final List<NamedCapability> KNOWN_CAPABILITIES = Arrays.asList(
        new NamedCapability("BLOBSTORE", BLOBSTORE),
        new NamedCapability("DATASTORE", DATASTORE),
        new NamedCapability("DATASTORE_WRITE", DATASTORE_WRITE),
        new NamedCapability("IMAGES", IMAGES),
        new NamedCapability("MAIL", MAIL),
        new NamedCapability("MEMCACHE", MEMCACHE),
        new NamedCapability("PROSPECTIVE_SEARCH", PROSPECTIVE_SEARCH),
        new NamedCapability("TASKQUEUE", TASKQUEUE),
        new NamedCapability("URL_FETCH", URL_FETCH),
        new NamedCapability("XMPP", XMPP));

    private ExposedCapabilitiesService service;

    @Inject
    @HttpParam("BLOBSTORE")
    private String blobstoreStatus;

    @Inject
    @HttpParam("DATASTORE")
    private String datastoreStatus;

    @Inject
    @HttpParam("DATASTORE_WRITE")
    private String datastoreWriteStatus;

    @Inject
    @HttpParam("IMAGES")
    private String imagesStatus;

    @Inject
    @HttpParam("MAIL")
    private String mailStatus;

    @Inject
    @HttpParam("MEMCACHE")
    private String memcacheStatus;

    @Inject
    @HttpParam("PROSPECTIVE_SEARCH")
    private String prospectiveSearchStatus;

    @Inject
    @HttpParam("TASKQUEUE")
    private String taskQueueStatus;

    @Inject
    @HttpParam("URL_FETCH")
    private String urlFetchStatus;

    @Inject
    @HttpParam("XMPP")
    private String xmppStatus;

    @PostConstruct
    public void init() {
        service = (ExposedCapabilitiesService) CapabilitiesServiceFactory.getCapabilitiesService();

        updateStatus(Capability.BLOBSTORE, blobstoreStatus);
        updateStatus(Capability.DATASTORE, datastoreStatus);
        updateStatus(Capability.DATASTORE_WRITE, datastoreWriteStatus);
        updateStatus(Capability.IMAGES, imagesStatus);
        updateStatus(Capability.MAIL, mailStatus);
        updateStatus(Capability.MEMCACHE, memcacheStatus);
        updateStatus(Capability.PROSPECTIVE_SEARCH, prospectiveSearchStatus);
        updateStatus(Capability.TASKQUEUE, taskQueueStatus);
        updateStatus(Capability.URL_FETCH, urlFetchStatus);
        updateStatus(Capability.XMPP, xmppStatus);
    }

    private void updateStatus(Capability capability, String statusName) {
        if (statusName == null) {
            return;
        }
        CapabilityStatus status = CapabilityStatus.valueOf(statusName);
        service.setCapabilityStatus(capability, status);
    }

    public List<NamedCapability> getCapabilities() {
        return KNOWN_CAPABILITIES;
    }

    public CapabilityStatus getStatus(Capability capability) {
        return service.getStatus(capability).getStatus();
    }

    public static class NamedCapability {
        private String name;
        private Capability capability;

        public NamedCapability(String name, Capability capability) {
            this.name = name;
            this.capability = capability;
        }

        public String getName() {
            return name;
        }

        public Capability getCapability() {
            return capability;
        }
    }
}
