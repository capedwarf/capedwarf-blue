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

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.appengine.api.NamespaceManager;
import com.google.apphosting.api.ApiProxy;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfEnvironment implements ApiProxy.Environment, Serializable {
    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<CapedwarfEnvironment> threadLocalInstance = new ThreadLocal<CapedwarfEnvironment>();
    public static final String DEFAULT_VERSION_HOSTNAME = "com.google.appengine.runtime.default_version_hostname";

    /* Impl detail ... */
    public static final String REQUEST_END_LISTENERS = "com.google.appengine.tools.development.request_end_listeners";
    private static final String REQUEST_THREAD_FACTORY_ATTR = "com.google.appengine.api.ThreadManager.REQUEST_THREAD_FACTORY";
    private static final String BACKGROUND_THREAD_FACTORY_ATTR = "com.google.appengine.api.ThreadManager.BACKGROUND_THREAD_FACTORY";

    private static final String HTTPS = "https";
    private static final String DELIMITER = "://";
    private static final int DEFAULT_HTTP_PORT = 80;

    private String email;
    private boolean isAdmin;
    private String authDomain;
    private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private CapedwarfConfiguration capedwarfConfiguration;
    private AppEngineWebXml appEngineWebXml;
    private QueueXml queueXml;
    private String baseApplicationUrl;
    private String secureBaseApplicationUrl;

    public CapedwarfEnvironment() {
        // a bit of a workaround for LocalServiceTestHelper::tearDown NPE
        attributes.put(REQUEST_END_LISTENERS, new ArrayList());
        // add thread factory
        attributes.put(REQUEST_THREAD_FACTORY_ATTR, LazyThreadFactory.INSTANCE);
        attributes.put(BACKGROUND_THREAD_FACTORY_ATTR, LazyThreadFactory.INSTANCE);
    }

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
        return isLoggedIn() && isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
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

    public QueueXml getQueueXml() {
        return queueXml;
    }

    public void setQueueXml(QueueXml queueXml) {
        this.queueXml = queueXml;
    }

    public Collection<String> getAdmins() {
        return capedwarfConfiguration.getAdmins();
    }

    public boolean isAdmin(String email) {
        return capedwarfConfiguration.isAdmin(email);
    }

    public void setBaseApplicationUrl(String scheme, String serverName, int port, String context) {
        String sPort = (port == DEFAULT_HTTP_PORT) ? "" : ":" + port;
        baseApplicationUrl = scheme + DELIMITER + serverName + sPort + context;
        if (HTTPS.equals(scheme)) {
            secureBaseApplicationUrl = baseApplicationUrl;
        } else {
            secureBaseApplicationUrl = HTTPS + DELIMITER + serverName + sPort + context;
        }
        attributes.put(DEFAULT_VERSION_HOSTNAME, baseApplicationUrl);
    }

    public String getBaseApplicationUrl() {
        return getBaseApplicationUrl(false);
    }

    public String getBaseApplicationUrl(boolean secureUrl) {
        return secureUrl ? secureBaseApplicationUrl : baseApplicationUrl;
    }

    public static CapedwarfEnvironment createThreadLocalInstance() {
        CapedwarfEnvironment environment = new CapedwarfEnvironment();
        threadLocalInstance.set(environment);
        return environment;
    }

    public static CapedwarfEnvironment getThreadLocalInstance() {
        final CapedwarfEnvironment environment = getThreadLocalInstanceInternal();
        if (environment == null) {
            throw new IllegalStateException("Environment should exist!");
        }
        return environment;
    }

    /**
     * This one doesn't make any check if env is null.
     *
     * @return env or null if not set
     */
    public static CapedwarfEnvironment getThreadLocalInstanceInternal() {
        return threadLocalInstance.get();
    }

    public static void clearThreadLocalInstance() {
        threadLocalInstance.remove();
    }

    public static CapedwarfEnvironment setThreadLocalInstance(final CapedwarfEnvironment env) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            CapedwarfEnvironment previous = threadLocalInstance.get();
            threadLocalInstance.set(env);
            return previous;
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<CapedwarfEnvironment>() {
                public CapedwarfEnvironment run() {
                    CapedwarfEnvironment previous = threadLocalInstance.get();
                    threadLocalInstance.set(env);
                    return previous;
                }
            });
        }
    }
}

