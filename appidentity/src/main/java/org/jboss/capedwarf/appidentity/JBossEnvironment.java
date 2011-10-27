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

package org.jboss.capedwarf.appidentity;

import com.google.appengine.api.NamespaceManager;
import com.google.apphosting.api.ApiProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossEnvironment implements ApiProxy.Environment {

    private static final ThreadLocal<JBossEnvironment> threadLocalInstance = new ThreadLocal<JBossEnvironment>();

    private String appId = "no-app-id";
    private String versionId;
    private Map<String, Object> attributes = new HashMap<String, Object>();

    public String getAppId() {
        return appId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getEmail() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLoggedIn() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAdmin() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAuthDomain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestNamespace() {
        return NamespaceManager.getGoogleAppsNamespace();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
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

