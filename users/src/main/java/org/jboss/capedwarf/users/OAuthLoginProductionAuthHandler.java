/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.users;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.capedwarf.common.io.Base64Utils;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.OAuthConfiguration;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class OAuthLoginProductionAuthHandler extends AuthHandler {

    private static final String STATE_ATTRIBUTE = "__capedwarf_oauth_login_state";
    private static final String DESTINATION_URL_ATTRIBUTE = "__capedwarf_oauth_login_destination_url";
    private static final String GMAIL_COM = "gmail.com";
    private static final String URLS_ENDPOINT = "https://accounts.google.com/.well-known/openid-configuration";

    private ObjectMapper objectMapper = new ObjectMapper();

    private volatile Urls urls;

    public OAuthLoginProductionAuthHandler() {
        validateConfiguration();
    }

    private void validateConfiguration() {
        if (getClientId() == null) {
            throw new IllegalStateException("OAuth clientId not configured in capedwarf-web.xml");
        }
        if (getClientSecret() == null) {
            throw new IllegalStateException("OAuth clientSecret not configured in capedwarf-web.xml");
        }
    }

    private Urls getUrls() {
        if (urls == null) {
            synchronized (this) {
                if (urls == null) {
                    ResteasyClient client = new ResteasyClientBuilder().build();
                    ResteasyWebTarget target = client.target(URLS_ENDPOINT);
                    Response response = target.request().accept("application/json").get();
                    urls = response.readEntity(Urls.class);
                }
            }
        }
        return urls;
    }

    public void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authDomain = request.getParameter(AuthServlet.AUTH_DOMAIN_PARAM);

        String state = new BigInteger(130, new SecureRandom()).toString(32);

        String destinationUrl = request.getParameter(AuthServlet.DESTINATION_URL_PARAM);
        request.getSession().setAttribute(DESTINATION_URL_ATTRIBUTE, destinationUrl);
        request.getSession().setAttribute(STATE_ATTRIBUTE, state);
        String endpointUrl = getFullAuthorizationEndpointUrl(authDomain, state);
        ServletUtils.redirect(request, response, endpointUrl);
    }

    private String getFullAuthorizationEndpointUrl(String authDomain, String state) {
        return getAuthorizationEndpoint()
            + "?redirect_uri=" + getRedirectUrl()
            + "&client_id=" + getClientId()
            + "&scope=openid email"
            + "&response_type=code"
            + "&state=" + state
            + (authDomain == null ? "" : ("&hd=" + authDomain));
    }

    private String getRedirectUrl() {
        return getReturnUrl();
    }

    @Override
    public void handleOpenIDCallBackRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = request.getParameter("state");
        if (!state.equals(request.getSession().getAttribute(STATE_ATTRIBUTE))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String code = request.getParameter("code");
        try {
            GoogleIdToken token = obtainGoogleIdToken(code);

            String email = token.getEmail();
            String userId = token.getSubject();
            String authDomain = GMAIL_COM;    // appspot always returns gmail.com, even for custom domains

            boolean isAdmin = ApplicationConfiguration.getInstance().getCapedwarfConfiguration().isAdmin(email);
            setupUserPrincipal(request, email, userId, authDomain, isAdmin);

            String destination = (String) request.getSession().getAttribute(DESTINATION_URL_ATTRIBUTE);
            ServletUtils.forward(request, response, destination);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private GoogleIdToken obtainGoogleIdToken(String code) throws IOException {
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(getTokenEndpoint());
        Form form = new Form()
            .param("code", code)
            .param("client_id", getClientId())
            .param("client_secret", getClientSecret())
            .param("redirect_uri", getRedirectUrl())
            .param("grant_type", "authorization_code");
        Response clientResponse = target.request().accept("application/json").post(Entity.form(form));
        OAuthLoginResponse oauthLoginResponse = clientResponse.readEntity(OAuthLoginResponse.class);
        String[] jwtParts = oauthLoginResponse.getIdToken().split("\\.");
        String content = jwtParts[1];
        return objectMapper.readValue(Base64Utils.decodeWebSafe(content), GoogleIdToken.class);
    }

    private String getAuthorizationEndpoint() {
        return getUrls().getAuthorizationEndpoint();
    }

    private String getTokenEndpoint() {
        return getUrls().getTokenEndpoint();
    }

    private String getReturnUrl() {
        return AuthServlet.getServletUrl() + AuthServlet.CALLBACK_PATH;
    }

    private String getClientId() {
        return getOAuthConfig().getClientId();
    }

    public String getClientSecret() {
        return getOAuthConfig().getClientSecret();
    }

    private OAuthConfiguration getOAuthConfig() {
        return ApplicationConfiguration.getInstance().getCapedwarfConfiguration().getOAuthConfiguration();
    }
}
