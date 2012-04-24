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

package org.jboss.capedwarf.oauth;

import com.google.appengine.api.oauth.InvalidOAuthParametersException;
import com.google.appengine.api.oauth.InvalidOAuthTokenException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.users.User;
import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * JBoss OAuth service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfOAuthService implements OAuthService {

    private static final String OAUTH_USER_REQUEST_ATTRIBUTE_PREFIX = "__CapeDwarf_OAuth_User_";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    public static final String USERINFO_API_URL = "https://www.googleapis.com/oauth2/v1/userinfo";

    public User getCurrentUser() throws OAuthRequestException {
        return getCurrentUser("");
    }

    public User getCurrentUser(String scope) throws OAuthRequestException {
        return getCurrentUser(new String[]{scope});
    }

    public User getCurrentUser(String... scopes) throws OAuthRequestException {
        HttpServletRequest request = (HttpServletRequest) CapedwarfDelegate.INSTANCE.getServletRequest();
        for (String scope : scopes) {
            User user = (User) request.getAttribute(OAUTH_USER_REQUEST_ATTRIBUTE_PREFIX + scope);
            if (user != null) {
                return user;
            }
            user = obtainOAuthUser(request, scope);
            if (user != null) {
                request.setAttribute(OAUTH_USER_REQUEST_ATTRIBUTE_PREFIX + scope, user);
                return user;
            }
        }
        return null;
    }

    private User obtainOAuthUser(HttpServletRequest request, String scope) throws OAuthRequestException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            if ("".equals(scope)) {
                throw new InvalidOAuthTokenException("Empty scope");
            } else if (USERINFO_EMAIL_SCOPE.equals(scope)) {
                String token = authorization.substring(BEARER_PREFIX.length());
                try {
                    ResteasyClient client = new ResteasyClientBuilder().build();
                    ResteasyWebTarget target = client.target(USERINFO_API_URL + "?access_token=" + token);
                    Response clientResponse = target.request().accept("application/json").get();
                    UserInfoResponse userInfoResponse = clientResponse.readEntity(UserInfoResponse.class);
                    return new User(userInfoResponse.getEmail(), "gmail.com", userInfoResponse.getId());
                } catch (Exception e) {
                    throw new OAuthRequestException(e.toString());
                }
            } else {
                throw new InvalidOAuthParametersException("incorrect scopes");
            }
        }
        throw new InvalidOAuthParametersException("");
    }

    public boolean isUserAdmin() throws OAuthRequestException {
        return isUserAdmin("");
    }

    @Override
    public boolean isUserAdmin(String scope) throws OAuthRequestException {
        return isUserAdmin(new String[]{scope});
    }

    public boolean isUserAdmin(String... scopes) throws OAuthRequestException {
        User currentUser = getCurrentUser(scopes);
        if (currentUser == null) {
            throw new IllegalStateException("There is no current user.");
        }

        CapedwarfEnvironment environment = (CapedwarfEnvironment) ApiProxy.getCurrentEnvironment();
        return environment.getCapedwarfConfiguration().isAdmin(currentUser.getEmail());
    }

    public String getOAuthConsumerKey() throws OAuthRequestException {
        return null;  // TODO
    }

    @Override
    public String getClientId(String scope) throws OAuthRequestException {
        return getClientId(new String[]{scope});
    }

    public String getClientId(String... scopes) throws OAuthRequestException {
        return null; // TODO
    }

    public String[] getAuthorizedScopes(String... scopes) throws OAuthRequestException {
        return new String[0];
    }

}
