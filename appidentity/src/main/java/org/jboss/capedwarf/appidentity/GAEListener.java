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

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.log.LogServiceFactory;
import org.jboss.capedwarf.common.config.AppEngineWebXml;
import org.jboss.capedwarf.common.config.AppEngineWebXmlParser;
import org.jboss.capedwarf.common.config.CapedwarfConfiguration;
import org.jboss.capedwarf.common.config.CapedwarfConfigurationParser;
import org.jboss.capedwarf.common.config.JBossEnvironment;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.log.JBossLogService;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GAEListener implements ServletContextListener, ServletRequestListener {

    private static Logger log = Logger.getLogger(GAEListener.class.getName());

    private static final String APPENGINE_WEB_XML = "/WEB-INF/appengine-web.xml";
    private static final String CAPEDWARF_WEB_XML = "/WEB-INF/capedwarf-web.xml";

    private ServletContext servletContext;

    private AppEngineWebXml appEngineWebXml;
    private CapedwarfConfiguration capedwarfConfiguration;

    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();

        try {
            appEngineWebXml = readAppEngineWebXml();
            capedwarfConfiguration = readCapedwarfConfig();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read configuration files", e);
        }

        final String appId = appEngineWebXml.getApplication();
        InfinispanUtils.initApplicationData(appId);
        servletContext.setAttribute("org.jboss.capedwarf.appId", appId);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        final String appId = appEngineWebXml.getApplication();
        InfinispanUtils.clearApplicationData(appId);
    }

    public void requestInitialized(ServletRequestEvent sre) {
        long requestStartMillis = System.currentTimeMillis();

        final ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest)
            initJBossEnvironment((HttpServletRequest) req);

        getLogService().requestStarted(sre.getServletRequest(), requestStartMillis);
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        getLogService().requestFinished(sre.getServletRequest());
        clearJBossEnvironment();
    }

    private JBossLogService getLogService() {
        return ((JBossLogService) LogServiceFactory.getLogService());
    }

    private AppEngineWebXml readAppEngineWebXml() throws IOException {
        InputStream stream = getWebResourceAsStream(APPENGINE_WEB_XML);
        if (stream == null)
            throw new IOException("No appengine-web.xml found: " + servletContext);

        try {
            return AppEngineWebXmlParser.parse(stream);
        } finally {
            stream.close();
        }
    }

    private CapedwarfConfiguration readCapedwarfConfig() throws IOException {
        InputStream stream = getWebResourceAsStream(CAPEDWARF_WEB_XML);
        if (stream == null) {
            log.info("No capedwarf-web.xml found.");
            return new CapedwarfConfiguration();
        }

        try {
            return CapedwarfConfigurationParser.parse(stream);
        } finally {
            IOUtils.safeClose(stream);
        }
    }


    private InputStream getWebResourceAsStream(String path) {
        return servletContext.getResourceAsStream(path);
    }

    private void initJBossEnvironment(HttpServletRequest request) {
        JBossEnvironment environment = JBossEnvironment.getThreadLocalInstance();
        initApplicationData(environment);
        initRequestData(environment, request);
        initUserData(environment, request);
    }

    private void initApplicationData(JBossEnvironment environment) {
        environment.setAppEngineWebXml(appEngineWebXml);
        environment.setCapedwarfConfiguration(capedwarfConfiguration);
    }

    private void initRequestData(JBossEnvironment environment, HttpServletRequest request) {
        environment.setBaseApplicationUrl(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
    }

    private void initUserData(JBossEnvironment environment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            environment.setEmail(principal.getName());
//            environment.setAuthDomain();
        }
    }

    private void clearJBossEnvironment() {
        JBossEnvironment.clearThreadLocalInstance();
    }

    public void destroy() {
    }
}
