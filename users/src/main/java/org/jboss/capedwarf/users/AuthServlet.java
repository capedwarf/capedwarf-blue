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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.capedwarf.appidentity.CapedwarfHttpServletRequestWrapper;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.url.URLUtils;
import org.picketlink.social.openid.api.OpenIDManager;
import org.picketlink.social.openid.api.OpenIDProtocolAdapter;
import org.picketlink.social.openid.api.OpenIDRequest;
import org.picketlink.social.openid.api.exceptions.OpenIDAssociationException;
import org.picketlink.social.openid.api.exceptions.OpenIDDiscoveryException;
import org.picketlink.social.openid.api.exceptions.OpenIDGeneralException;
import org.picketlink.social.openid.api.exceptions.OpenIDLifeCycleException;
import org.picketlink.social.openid.api.exceptions.OpenIDMessageException;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@WebServlet(urlPatterns = AuthServlet.SERVLET_URI + "/*")
public class AuthServlet extends HttpServlet {

    private static final String GOOGLE_OPEN_ID_SERVICE_URL = "https://www.google.com/accounts/o8/id";

    private static final String OPENID_MANAGER_KEY = "openid_manager";

    public static final String SERVLET_URI = "/_ah/auth";

    private static final String LOGIN_RETURN_PATH = "/login_return";
    private static final String LOGIN_PATH = "/login";
    private static final String LOGOUT_PATH = "/logout";

    public static final String FEDERATED_IDENTITY_PARAM = "federatedIdentity";
    public static final String DESTINATION_URL_PARAM = "destinationURL";
    public static final String AUTH_DOMAIN_PARAM = "authDomain";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo.equals(LOGIN_RETURN_PATH)) {
            handleOpenIdCallBack(request, response);
        } else if (pathInfo.equals(LOGIN_PATH)) {
            handleLoginRequest(request, response);
        } else if (pathInfo.equals(LOGOUT_PATH)) {
            handleLogoutRequest(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getSession().setAttribute(CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY, null);

            String destinationUrl = request.getParameter(DESTINATION_URL_PARAM);
            response.sendRedirect(destinationUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authDomain = request.getParameter(AUTH_DOMAIN_PARAM);    // TODO: what is authDomain _exactly_?

        OpenIDManager manager = getOpenIdManager(request);
        try {
            OpenIDProtocolAdapter adapter = createOpenIdProtocolAdapter(request, response);
            OpenIDManager.OpenIDProviderInformation providerInfo = manager.associate(adapter, manager.discoverProviders());
            manager.authenticate(adapter, providerInfo);

        } catch (OpenIDGeneralException e) {
            log("[OpenIDConsumerServlet] Exception in dealing with the provider:", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getReturnUrl(HttpServletRequest request) {
        String destinationURL = request.getParameter(DESTINATION_URL_PARAM);
        return getServletUrl()
                + LOGIN_RETURN_PATH
                + "?" + DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL);
    }

    private void handleOpenIdCallBack(final HttpServletRequest request, final HttpServletResponse response) {
        // extract the receiving URL from the HTTP request
        try {
            OpenIDProtocolAdapter adapter = createOpenIdProtocolAdapter(request, response);
            OpenIDManager manager = getOpenIdManager(request.getSession());
            boolean auth = manager.verify(adapter, getStringToStringParameterMap(request), getFullRequestURL(request));

        } catch (OpenIDMessageException e) {
            throw new RuntimeException(e);
        } catch (OpenIDDiscoveryException e) {
            throw new RuntimeException(e);
        } catch (OpenIDAssociationException e) {
            throw new RuntimeException(e);
        } catch (OpenIDLifeCycleException e) {
            throw new RuntimeException(e);
        }
    }

    private CapedwarfOpenIDProtocolAdaptor createOpenIdProtocolAdapter(HttpServletRequest request, HttpServletResponse response) {
        return new CapedwarfOpenIDProtocolAdaptor(request, response, getReturnUrl(request));
    }

    private Map<String, String> getStringToStringParameterMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue().length > 0 ? entry.getValue()[0] : null);
        }
        return map;
    }

    private String getFullRequestURL(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURL().toString()
                + ((queryString == null || queryString.isEmpty()) ? "" : ("?" + queryString));
    }

    private OpenIDManager getOpenIdManager(HttpServletRequest req) {
        OpenIDManager manager = getOpenIdManager(req.getSession());
        if (manager == null) {
            manager = new OpenIDManager(createOpenIdRequest(req));
            req.getSession().setAttribute(OPENID_MANAGER_KEY, manager);
        }
        return manager;
    }

    private OpenIDRequest createOpenIdRequest(HttpServletRequest req) {
        String federatedIdentity = req.getParameter(FEDERATED_IDENTITY_PARAM);
        String openIdUrl = federatedIdentity == null || federatedIdentity.isEmpty() ? GOOGLE_OPEN_ID_SERVICE_URL : federatedIdentity;
        return new OpenIDRequest(openIdUrl);
    }

    private OpenIDManager getOpenIdManager(HttpSession session) {
        return (OpenIDManager) session.getAttribute(OPENID_MANAGER_KEY);
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

    private static String getServletUrl() {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl() + SERVLET_URI;
    }

}
