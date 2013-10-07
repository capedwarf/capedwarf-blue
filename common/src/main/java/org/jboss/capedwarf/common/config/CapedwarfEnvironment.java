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
import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Function;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.BackendsXml;
import org.jboss.capedwarf.shared.config.CapedwarfConfiguration;
import org.jboss.capedwarf.shared.config.IndexesXml;
import org.jboss.capedwarf.shared.config.QueueXml;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfEnvironment implements ApiProxy.Environment, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_VERSION_HOSTNAME = "com.google.appengine.runtime.default_version_hostname";

    public static final String USER_ID_KEY = "com.google.appengine.api.users.UserService.user_id_key";
    public static final String IS_FEDERATED_USER_KEY = "com.google.appengine.api.users.UserService.is_federated_user";
    public static final String FEDERATED_AUTHORITY_KEY = "com.google.appengine.api.users.UserService.federated_authority";
    public static final String FEDERATED_IDENTITY_KEY = "com.google.appengine.api.users.UserService.federated_identity";

    /* Impl detail ... */
    public static final String REQUEST_END_LISTENERS = "com.google.appengine.tools.development.request_end_listeners";
    private static final String REQUEST_THREAD_FACTORY_ATTR = "com.google.appengine.api.ThreadManager.REQUEST_THREAD_FACTORY";
    private static final String BACKGROUND_THREAD_FACTORY_ATTR = "com.google.appengine.api.ThreadManager.BACKGROUND_THREAD_FACTORY";

    private static final String HTTPS = "https";
    private static final String DELIMITER = "://";
    private static final int DEFAULT_HTTP_PORT = 80;

    private static final long GLOBAL_TIME_LIMIT = Long.parseLong(System.getProperty("jboss.capedwarf.globalTimeLimit", "60000"));

    private final long requestStart;
    private final Map<String, Object> attributes;

    private final transient Function<BackendsXml.Backend, String> ADDRESS_FN = new Function<BackendsXml.Backend, String>() {
        @Override
        public String apply(BackendsXml.Backend input) {
            return getDefaultVersionHostname();
        }
    };

    private volatile Boolean checkGlobalTimeLimit;

    private String email;
    private boolean isAdmin;
    private String authDomain;

    private CapedwarfConfiguration capedwarfConfiguration;
    private AppEngineWebXml appEngineWebXml;
    private QueueXml queueXml;
    private BackendsXml backends;
    private IndexesXml indexes;

    private String baseApplicationUrl;
    private String secureBaseApplicationUrl;
    private String defaultVersionHostname;
    private String contextPath;

    private int counter;

    public CapedwarfEnvironment() {
        requestStart = System.currentTimeMillis();
        // attributes
        attributes = new ConcurrentHashMap<String, Object>();
        // a bit of a workaround for LocalServiceTestHelper::tearDown NPE
        attributes.put(REQUEST_END_LISTENERS, new ArrayList());
        // add thread factory
        attributes.put(REQUEST_THREAD_FACTORY_ATTR, LazyThreadFactory.INSTANCE);
        attributes.put(BACKGROUND_THREAD_FACTORY_ATTR, LazyThreadFactory.INSTANCE);
    }

    private boolean doCheckGlobalTimeLimit() {
        if (checkGlobalTimeLimit == null) {
            checkGlobalTimeLimit = CompatibilityUtils.getInstance().isEnabled(Compatibility.Feature.ENABLE_GLOBAL_TIME_LIMIT);
        }
        return checkGlobalTimeLimit;
    }

    public boolean isProduction() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
    }

    public void checkGlobalTimeLimit() {
        if (doCheckGlobalTimeLimit()) {
            checkTimeLimit(requestStart, GLOBAL_TIME_LIMIT);
        }
    }

    public void checkTimeLimit(long start, long limit) {
        long current = System.currentTimeMillis();
        long diff = current - start;
        if (diff > limit)
            throw new IllegalStateException("Execution taking too long: " + diff);
    }

    @Override
    public String getAppId() {
        assertInitialized();
        return appEngineWebXml.getApplication();
    }

    @Override
    public String getVersionId() {
        assertInitialized();
        return appEngineWebXml.getVersion();
    }

    @Override
    public String getModuleId() {
        assertInitialized();
        return appEngineWebXml.getModule();
    }

    private void assertInitialized() {
        if (appEngineWebXml == null)
            throw new IllegalStateException("Application data has not been initialized. Was this method called AFTER GAEFilter did its job?");
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isLoggedIn() {
        return email != null;
    }

    @Override
    public boolean isAdmin() {
        return isLoggedIn() && isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String getAuthDomain() {
        return authDomain;
    }

    @Override
    public String getRequestNamespace() {
        return NamespaceManager.getGoogleAppsNamespace();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public long getRemainingMillis() {
        return requestStart + GLOBAL_TIME_LIMIT - System.currentTimeMillis();
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

    public AppEngineWebXml getAppEngineWebXml() {
        return appEngineWebXml;
    }

    public QueueXml getQueueXml() {
        return queueXml;
    }

    public void setQueueXml(QueueXml queueXml) {
        this.queueXml = queueXml;
    }

    public BackendsXml getBackends() {
        return backends;
    }

    public void setBackends(BackendsXml backends) {
        this.backends = backends;
    }

    public IndexesXml getIndexes() {
        return indexes;
    }

    public void setIndexes(IndexesXml indexes) {
        this.indexes = indexes;
    }

    public Collection<String> getAdmins() {
        return capedwarfConfiguration.getAdmins();
    }

    public boolean isAdmin(String email) {
        return capedwarfConfiguration.isAdmin(email);
    }

    public void setBaseApplicationUrl(String scheme, String serverName, int port, String context) {
        String sPort = (port == DEFAULT_HTTP_PORT) ? "" : ":" + port;
        defaultVersionHostname = serverName + sPort + context;
        baseApplicationUrl = scheme + DELIMITER + defaultVersionHostname;
        if (HTTPS.equals(scheme)) {
            secureBaseApplicationUrl = baseApplicationUrl;
        } else {
            secureBaseApplicationUrl = HTTPS + DELIMITER + defaultVersionHostname;
        }
        attributes.put(DEFAULT_VERSION_HOSTNAME, defaultVersionHostname);
        contextPath = context;
    }

    public String getBaseApplicationUrl() {
        return getBaseApplicationUrl(false);
    }

    public String getBaseApplicationUrl(boolean secureUrl) {
        return secureUrl ? secureBaseApplicationUrl : baseApplicationUrl;
    }

    public String getDefaultVersionHostname() {
        return defaultVersionHostname;
    }

    public String getContextPath() {
        return contextPath;
    }

    public static CapedwarfEnvironment createThreadLocalInstance() {
        CapedwarfEnvironment environment = getThreadLocalInstanceInternal();
        if (environment == null) {
            environment = new CapedwarfEnvironment();
            ApiProxy.setEnvironmentForCurrentThread(environment);
        }
        environment.counter++;

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
     * Check if env already exists.
     *
     * @return true if already exists, false otherwise
     */
    public static boolean hasThreadLocalInstance() {
        return (getThreadLocalInstanceInternal() != null);
    }

    public static void clearThreadLocalInstance() {
        CapedwarfEnvironment env = getThreadLocalInstanceInternal();
        if (--env.counter == 0) {
            ApiProxy.clearEnvironmentForCurrentThread();
        }
        if (env.counter < 0) {
            Logger.getLogger(CapedwarfEnvironment.class.getName()).warning("Negative counter: " + env.counter + " !!");
        }
    }

    public static CapedwarfEnvironment setThreadLocalInstance(final CapedwarfEnvironment environment) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return setCapedwarfEnvironment(environment);
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<CapedwarfEnvironment>() {
                @Override
                public CapedwarfEnvironment run() {
                    return setCapedwarfEnvironment(environment);
                }
            });
        }
    }

    private static CapedwarfEnvironment setCapedwarfEnvironment(CapedwarfEnvironment environment) {
        CapedwarfEnvironment previous = getThreadLocalInstanceInternal();
        if (environment != null) {
            ApiProxy.setEnvironmentForCurrentThread(environment);
        } else {
            ApiProxy.clearEnvironmentForCurrentThread();
        }
        return previous;
    }

    /**
     * This one doesn't make any check if env is null.
     *
     * @return env or null if not set
     */
    protected static CapedwarfEnvironment getThreadLocalInstanceInternal() {
        return (CapedwarfEnvironment) ApiProxy.getCurrentEnvironment();
    }

    /**
     * Mark env as initialized.
     */
    public void initialized() {
        if (backends != null) {
            // TODO -- move this to CD_JBossAS
            attributes.put(BackendService.DEVAPPSERVER_PORTMAPPING_KEY, backends.getAddresses(ADDRESS_FN));
        }
    }

    public void setUserId(String userId) {
        getAttributes().put(USER_ID_KEY, userId);
    }
}

