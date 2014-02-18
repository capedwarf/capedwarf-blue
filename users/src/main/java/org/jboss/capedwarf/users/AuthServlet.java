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

package org.jboss.capedwarf.users;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.url.URLUtils;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Production;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class AuthServlet extends HttpServlet {
    public static final String FEDERATED_IDENTITY_PARAM = "federatedIdentity";
    public static final String DESTINATION_URL_PARAM = "continue";
    public static final String AUTH_DOMAIN_PARAM = "authDomain";

    public static final String LOGIN_PATH = "/_ah/login";
    public static final String LOGOUT_PATH = "/_ah/logout";
    public static final String CALLBACK_PATH = "/_ah/openIDCallBack";

    protected AuthHandler authHandler;

    public static String createLoginURL(String destinationURL, String authDomain, String federatedIdentity, Set<String> attributesRequest) {
        return addContextPath(LOGIN_PATH)
            + "?" + DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL)
            + (authDomain == null ? "" : ("&" + AUTH_DOMAIN_PARAM + "=" + URLUtils.encode(authDomain)))
            + (federatedIdentity == null ? "" : ("&" + FEDERATED_IDENTITY_PARAM + "=" + URLUtils.encode(federatedIdentity)));
    }

    public static String createLogoutURL(String destinationURL, String authDomain) {
        return addContextPath(LOGOUT_PATH)
            + "?" + DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL)
            + "&" + AUTH_DOMAIN_PARAM + "=" + URLUtils.encode(authDomain);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        SystemProperty.Environment.Value environment = SystemProperty.environment.value();
        if (environment == Production) {
            authHandler = new ProductionAuthHandler();
        } else if (environment == Development) {
            authHandler = new DevelopmentAuthHandler();
        } else {
            throw new IllegalStateException("Unknown environment: " + environment);
        }
    }

    protected static String getServletUrl() {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl();
    }

    protected static String addContextPath(String path) {
        String contextPath = CapedwarfEnvironment.getThreadLocalInstance().getContextPath();
        int length = contextPath.length();
        if (length == 0 || (length == 1 && contextPath.charAt(0) == '/')) {
            return path;
        } else {
            if (contextPath.startsWith("/")) {
                return contextPath + path;
            } else {
                return "/" + contextPath + path;
            }
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith(LOGIN_PATH)) {
            authHandler.handleLoginRequest(request, response);
        } else if (path.startsWith(LOGOUT_PATH)) {
            authHandler.handleLogoutRequest(request, response);
        } else if (path.startsWith(CALLBACK_PATH)) {
            authHandler.handleOpenIDCallBackRequest(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
