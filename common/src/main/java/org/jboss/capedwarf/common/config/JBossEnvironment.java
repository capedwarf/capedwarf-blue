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

package org.jboss.capedwarf.common.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.NamespaceManager;
import com.google.apphosting.api.ApiProxy;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossEnvironment implements ApiProxy.Environment {

    private static final ThreadLocal<JBossEnvironment> threadLocalInstance = new ThreadLocal<JBossEnvironment>();
    public static final String DEFAULT_VERSION_HOSTNAME = "com.google.appengine.runtime.default_version_hostname";

    private String email;
    private String authDomain;
    private Map<String, Object> attributes = new HashMap<String, Object>();

    private CapedwarfConfiguration capedwarfConfiguration;
    private AppEngineWebXml appEngineWebXml;
    private String baseApplicationUrl;

    public String getAppId() {
        assertInitialized();
        return appEngineWebXml.getApplication();
    }

    public String getVersionId() {
        assertInitialized();
        return appEngineWebXml.getVersion();
    }

    private void assertInitialized() {
        if (appEngineWebXml == null)
            throw new IllegalStateException("Application data has not been initialized. Was this method called AFTER GAEFilter did its job?");
    }

    public String getEmail() {
        return email;
    }

    public boolean isLoggedIn() {
        return email != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && capedwarfConfiguration.isAdmin(getEmail());
    }

    public String getAuthDomain() {
        return authDomain;
    }

    public String getRequestNamespace() {
        return NamespaceManager.getGoogleAppsNamespace();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public long getRemainingMillis() {
        return 0; // TODO
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain;
    }

    public void setCapedwarfConfiguration(CapedwarfConfiguration capedwarfConfiguration) {
        this.capedwarfConfiguration = capedwarfConfiguration;
    }

    public CapedwarfConfiguration getCapedwarfConfiguration() {
        return capedwarfConfiguration;
    }

    public void setAppEngineWebXml(AppEngineWebXml appEngineWebXml) {
        this.appEngineWebXml = appEngineWebXml;
    }

    public Collection<String> getAdmins() {
        return capedwarfConfiguration.getAdmins();
    }

    public void setBaseApplicationUrl(String baseApplicationUrl) {
        attributes.put(DEFAULT_VERSION_HOSTNAME, baseApplicationUrl);
        this.baseApplicationUrl = baseApplicationUrl;
    }

    public String getBaseApplicationUrl() {
        return baseApplicationUrl;
    }

    public static JBossEnvironment getThreadLocalInstance() {
        JBossEnvironment environment = threadLocalInstance.get();
        if (environment == null) {
            environment = new JBossEnvironment();
            threadLocalInstance.set(environment);
        }
        return environment;
    }

    public static void clearThreadLocalInstance() {
        threadLocalInstance.set(null);
    }
}

