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

import org.jboss.capedwarf.common.config.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class GAEFilter implements Filter {

    private static final int DEFAULT_HTTP_PORT = 80;

    private static final String APPENGINE_WEB_XML = "/WEB-INF/appengine-web.xml";
    private static final String CAPEDWARF_WEB_XML = "/WEB-INF/capedwarf-web.xml";

    private FilterConfig filterConfig;

    private AppEngineWebXml appEngineWebXml;
    private CapedwarfConfiguration capedwarfConfiguration;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        try {
            appEngineWebXml = readAppEngineWebXml();
            capedwarfConfiguration = readCapedwarfConfig();
        } catch (Exception e) {
            throw new ServletException("Unable to read configuration files", e);
        }
    }

    private AppEngineWebXml readAppEngineWebXml() throws IOException {
        InputStream stream = getWebResourceAsStream(APPENGINE_WEB_XML);
        try {
            return AppEngineWebXmlParser.parse(stream);
        } finally {
            stream.close();
        }
    }

    private CapedwarfConfiguration readCapedwarfConfig() throws IOException {
        InputStream stream = getWebResourceAsStream(CAPEDWARF_WEB_XML);
        try {
            return CapedwarfConfigurationParser.parse(stream);
        } finally {
            stream.close();
        }
    }


    private InputStream getWebResourceAsStream(String path) {
        return filterConfig.getServletContext().getResourceAsStream(path);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        initJBossEnvironment((HttpServletRequest) request);
        try {
            chain.doFilter(request, response);
        } finally {
            clearJBossEnvironment();
        }
    }

    private void initJBossEnvironment(HttpServletRequest request) {
        JBossEnvironment environment = JBossEnvironment.getThreadLocalInstance();
        initApplicationData(environment);
        initRequestData(environment, request);
    }

    private void initApplicationData(JBossEnvironment environment) {
        environment.setAppEngineWebXml(appEngineWebXml);
        environment.setCapedwarfConfiguration(capedwarfConfiguration);
    }

    private void initRequestData(JBossEnvironment environment, HttpServletRequest request) {
        environment.setBaseApplicationUrl(request.getScheme() + "://"
                + request.getServerName()
                + (request.getServerPort() == DEFAULT_HTTP_PORT ? "" : (":" + request.getServerPort()))
                + request.getContextPath());
    }

    private void clearJBossEnvironment() {
        JBossEnvironment.clearThreadLocalInstance();
    }

    public void destroy() {
    }
}
