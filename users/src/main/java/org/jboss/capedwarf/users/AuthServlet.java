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
import javax.servlet.annotation.WebServlet;
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
@WebServlet(urlPatterns = AuthServlet.SERVLET_URI + "/*")
public class AuthServlet extends HttpServlet {

    public static final String SERVLET_URI = "/_ah/auth";

    private static final String LOGIN_PATH = "/login";
    private static final String LOGOUT_PATH = "/logout";

    public static final String FEDERATED_IDENTITY_PARAM = "federatedIdentity";
    public static final String DESTINATION_URL_PARAM = "destinationURL";
    public static final String AUTH_DOMAIN_PARAM = "authDomain";

    private AuthHandler authHandler;

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

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo.equals(LOGIN_PATH)) {
            authHandler.handleLoginRequest(request, response);
        } else if (pathInfo.equals(LOGOUT_PATH)) {
            authHandler.handleLogoutRequest(request, response);
        } else {
            authHandler.handleOtherRequest(request, response);
        }
    }

    public static String createLoginURL(String destinationURL, String authDomain, String federatedIdentity, Set<String> attributesRequest) {
        return getServletUrl()
                + LOGIN_PATH
                + "?" + DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL)
                + (authDomain == null ? "" : ("&" + AUTH_DOMAIN_PARAM + "=" + URLUtils.encode(authDomain)))
                + (federatedIdentity == null ? "" : ("&" + FEDERATED_IDENTITY_PARAM + "=" + URLUtils.encode(federatedIdentity)));
    }

    public static String createLogoutURL(String destinationURL, String authDomain) {
        return getServletUrl()
                + LOGOUT_PATH
                + "?" + DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL)
                + "&" + AUTH_DOMAIN_PARAM + "=" + URLUtils.encode(authDomain);
    }

    protected static String getServletUrl() {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl() + SERVLET_URI;
    }

}
