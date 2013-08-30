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

package org.jboss.capedwarf.appidentity;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.LifecycleManager;
import com.google.appengine.api.log.LogServiceFactory;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.security.PrincipalInfo;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.log.ExposedLogService;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.BackendsXml;
import org.jboss.capedwarf.shared.config.CapedwarfConfiguration;
import org.jboss.capedwarf.shared.config.ConfigurationAware;
import org.jboss.capedwarf.shared.config.IndexesXml;
import org.jboss.capedwarf.shared.config.QueueXml;

/**
 * Env setup is done in AS' CapedwarfSetupAction.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GAEListener extends ConfigurationAware implements ServletContextListener, ServletRequestListener {
    private volatile ExposedLogService logService;

    // Invoked from CapedwarfSetupAction -- do not change signatures; reflection usage!

    public static void setup() {
        setupInternal(appEngineWebXmlTL.get(), capedwarfConfigurationTL.get(), queueXmlTL.get(), backendsTL.get(), indexesTL.get());
    }

    public static boolean isSetup() {
        return CapedwarfEnvironment.hasThreadLocalInstance();
    }

    public static void teardown() {
        final CapedwarfEnvironment env = CapedwarfEnvironment.getThreadLocalInstance();
        try {
            env.checkGlobalTimeLimit();
        } finally {
            CapedwarfEnvironment.clearThreadLocalInstance();
        }
    }

    protected static void setupInternal(AppEngineWebXml appEngineWebXml, CapedwarfConfiguration capedwarfConfiguration, QueueXml queueXml, BackendsXml backends, IndexesXml indexes) {
        CapedwarfEnvironment environment = CapedwarfEnvironment.createThreadLocalInstance();
        environment.setAppEngineWebXml(appEngineWebXml);
        environment.setCapedwarfConfiguration(capedwarfConfiguration);
        environment.setQueueXml(queueXml);
        environment.setBackends(backends);
        environment.setIndexes(indexes);
    }

    // Servlet / Request event handling

    public void contextInitialized(ServletContextEvent sce) {
        initialize();

        final String appId = appEngineWebXml.getApplication();

        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("org.jboss.capedwarf.appId", appId);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String deadlineParameter = servletContext.getInitParameter("lifecycle-manager-deadline");
        long deadline = Long.parseLong((deadlineParameter != null) ? deadlineParameter : "0");
        LifecycleManager.getInstance().beginShutdown(deadline);
    }

    public void requestInitialized(ServletRequestEvent sre) {
        long requestStartMillis = System.currentTimeMillis();

        final ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest) {
            initJBossEnvironment((HttpServletRequest) req);
        }

        AppIdFactory.setCurrentFactory(EnvAppIdFactory.INSTANCE);

        CapedwarfDelegate.INSTANCE.addRequest(req);

        getLogService().requestStarted(req, requestStartMillis);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        final ServletRequest req = sre.getServletRequest();
        try {
            try {
                CapedwarfHttpServletResponseWrapper response = (CapedwarfHttpServletResponseWrapper) req.getAttribute(CapedwarfHttpServletResponseWrapper.class.getName());
                if (response != null) {
                    getLogService().requestFinished(req, response.getStatus(), response.getContentLength());
                } else {
                    // TODO -- looks like some error before GAEFilter kicked in.
                }
            } finally {
                CapedwarfDelegate.INSTANCE.removeRequest();
            }
        } finally {
            AppIdFactory.resetCurrentFactory();
        }
    }

    private void initJBossEnvironment(HttpServletRequest request) {
        CapedwarfEnvironment environment = CapedwarfEnvironment.getThreadLocalInstance();
        initRequestData(environment, request);
        initUserData(environment, request);
        environment.initialized();
    }

    private void initRequestData(CapedwarfEnvironment environment, HttpServletRequest request) {
        environment.setBaseApplicationUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
    }

    private void initUserData(CapedwarfEnvironment environment, HttpServletRequest request) {
        HttpSession session = request.getSession();
        // our fake request doesn't create session
        if (session != null) {
            PrincipalInfo principal = (PrincipalInfo) session.getAttribute(CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY);
            if (principal != null) {
                environment.setUserId(principal.getUserId());
                environment.setEmail(principal.getName());
                environment.setAuthDomain(principal.getAuthDomain());
                environment.setAdmin(principal.isAdmin());
            }
        }
    }

    private ExposedLogService getLogService() {
        if (logService == null) {
            logService = (ExposedLogService) LogServiceFactory.getLogService();
        }
        return logService;
    }
}
