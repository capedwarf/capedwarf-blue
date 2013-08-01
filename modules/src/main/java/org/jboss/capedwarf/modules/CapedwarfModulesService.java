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

package org.jboss.capedwarf.modules;

import java.util.Set;

import com.google.appengine.api.labs.modules.ModulesService;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfModulesService implements ModulesService {
    private static AppEngineWebXml getAppEngineWebXml() {
        return CapedwarfEnvironment.getThreadLocalInstance().getAppEngineWebXml();
    }

    public String getCurrentModule() {
        return getAppEngineWebXml().getModule();
    }

    public String getCurrentVersion() {
        return getAppEngineWebXml().getVersion();
    }

    public String getCurrentInstanceId() {
        return getAppEngineWebXml().getApplication();
    }

    public Set<String> getModules() {
        return null;
    }

    public Set<String> getVersions(String module) {
        return null;
    }

    public String getDefaultVersion(String module) {
        return null;
    }

    public long getNumInstances(String module, String version) {
        return 0;
    }

    public void setNumInstances(String module, String version, long instances) {
    }

    public void startModule(String module, String version) {
        // DMR?
    }

    public void stopModule(String module, String version) {
        // DMR?
    }

    public String getModuleHostname(String module, String version) {
        return null;
    }

    public String getModuleHostname(String module, String version, int instances) {
        return null;
    }
}
