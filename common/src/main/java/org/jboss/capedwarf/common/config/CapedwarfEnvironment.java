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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Function;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.BackendsXml;
import org.jboss.capedwarf.shared.config.CapedwarfConfiguration;
import org.jboss.capedwarf.shared.config.CheckType;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfEnvironment implements ApiProxy.Environment, Serializable, Cloneable, Function<BackendsXml.Backend, String> {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_VERSION_HOSTNAME = "com.google.appengine.runtime.default_version_hostname";
    public static final String INSTANCE_ID = "com.google.appengine.instance.id";

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
    private Map<String, Object> attributes;

    private volatile Boolean checkGlobalTimeLimit;

    private String email;
    private boolean isAdmin;
    private String authDomain;

    private ApplicationConfiguration applicationConfiguration;

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

    public String apply(BackendsXml.Backend input) {
        return getDefaultVersionHostname();
    }

    private boolean doCheckGlobalTimeLimit() {
        if (checkGlobalTimeLimit == null) {
            if (applicationConfiguration == null ||
                    applicationConfiguration.getCapedwarfConfiguration() == null ||
                    applicationConfiguration.getCapedwarfConfiguration().getCheckGlobalTimeLimit() == CheckType.NO) {
                checkGlobalTimeLimit = false;
            } else {
                CapedwarfConfiguration cc = applicationConfiguration.getCapedwarfConfiguration();
                if (cc.getCheckGlobalTimeLimit() == CheckType.DYNAMIC) {
                    Compatibility instance = Compatibility.getRawInstance();
                    // we could be in the middle of undeploy?
                    checkGlobalTimeLimit = instance != null && instance.isEnabled(Compatibility.Feature.ENABLE_GLOBAL_TIME_LIMIT);
                } else {
                    checkGlobalTimeLimit = true;
                }
            }
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
        return getAppEngineWebXml().getApplication();
    }

    @Override
    public String getVersionId() {
        return getAppEngineWebXml().getVersion() + ".1"; // TODO?
    }

    @Override
    public String getModuleId() {
        return getAppEngineWebXml().getModule();
    }

    private AppEngineWebXml getAppEngineWebXml() {
        return getApplicationConfiguration().getAppEngineWebXml();
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


    public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    private String getProperty(String prefix) {
        SystemProperty.Environment.Value env = SystemProperty.environment.value();
        if (env != null) {
            String key = String.format(prefix + ".%s", env.value().toLowerCase());
            String value = getApplicationConfiguration().getCapedwarfConfiguration().getProperties().getProperty(key);
            if (value != null) {
                return value;
            }
        }
        return getApplicationConfiguration().getCapedwarfConfiguration().getProperties().getProperty(prefix);
    }

    public void setBaseApplicationUrl(String scheme, String serverName, int port, String context) {
        String baseAppUrl = getProperty("base.app.url");
        if (baseAppUrl != null) {
            baseApplicationUrl = baseAppUrl;
            defaultVersionHostname = baseAppUrl.substring(baseAppUrl.indexOf(DELIMITER) + DELIMITER.length());
        }
        String secureAppUrl = getProperty("secure.app.url");
        if (secureAppUrl != null) {
            secureBaseApplicationUrl = secureAppUrl;
        }
        String path = getProperty("context.path");
        if (path != null) {
            contextPath = path;
        }

        if (contextPath == null) {
            contextPath = context;
        }

        if (baseApplicationUrl == null) {
            String sPort = (port == DEFAULT_HTTP_PORT) ? "" : ":" + port;
            defaultVersionHostname = serverName + sPort + contextPath;
            baseApplicationUrl = scheme + DELIMITER + defaultVersionHostname;
        }

        if (secureBaseApplicationUrl == null) {
            if (HTTPS.equals(scheme) || baseApplicationUrl.startsWith(HTTPS)) {
                secureBaseApplicationUrl = baseApplicationUrl;
            } else {
                secureBaseApplicationUrl = HTTPS + DELIMITER + defaultVersionHostname;
            }
        }

        attributes.put(DEFAULT_VERSION_HOSTNAME, defaultVersionHostname);
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

    public static CapedwarfEnvironment cloneThreadLocalInstance() {
        return getThreadLocalInstance().clone();
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
        BackendsXml backends = getApplicationConfiguration().getBackendsXml();
        if (backends != null) {
            // TODO -- move this to CD_JBossAS
            attributes.put(BackendService.DEVAPPSERVER_PORTMAPPING_KEY, backends.getAddresses(this));
        }
    }

    public void setUserId(String userId) {
        getAttributes().put(USER_ID_KEY, userId);
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected CapedwarfEnvironment clone() {
        CapedwarfEnvironment clone;
        try {
            clone = (CapedwarfEnvironment) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.attributes = new ConcurrentHashMap<>(attributes);

        return clone;
    }
}

