/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.util.Map;
import java.util.Set;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfUserService implements UserService {

    public String createLoginURL(String destinationURL) {
        return createLoginURL(destinationURL, null);
    }

    public String createLoginURL(String destinationURL, String authDomain) {
        return createLoginURL(destinationURL, authDomain, null, null);
    }

    public String createLoginURL(String destinationURL, String authDomain, String federatedIdentity, Set<String> attributesRequest) {
        return AuthServlet.createLoginURL(destinationURL, authDomain, federatedIdentity, attributesRequest);
    }

    public String createLogoutURL(String destinationURL) {
        return createLogoutURL(destinationURL, null);
    }

    public String createLogoutURL(String destinationURL, String authDomain) {
        return AuthServlet.createLogoutURL(destinationURL, authDomain);
    }

    public boolean isUserLoggedIn() {
        return ApiProxy.getCurrentEnvironment().isLoggedIn();
    }

    public boolean isUserAdmin() {
        if (isUserLoggedIn())
            return ApiProxy.getCurrentEnvironment().isAdmin();
        else
            throw new IllegalStateException("The current user is not logged in.");
    }

    public User getCurrentUser() {
        ApiProxy.Environment environment = ApiProxy.getCurrentEnvironment();
        if (!environment.isLoggedIn())
            return null;

        String userId = getEnvAttribute(CapedwarfEnvironment.USER_ID_KEY);
        Boolean isFederated = getEnvAttribute(CapedwarfEnvironment.IS_FEDERATED_USER_KEY);

        if (isFederated == null || !isFederated) {
            String authDomain = environment.getAuthDomain() == null ? "" : environment.getAuthDomain();
            return new User(environment.getEmail(), authDomain, userId);
        } else
            return new User(
                    environment.getEmail(),
                    (String) getEnvAttribute(CapedwarfEnvironment.FEDERATED_AUTHORITY_KEY),
                    userId,
                    (String) getEnvAttribute(CapedwarfEnvironment.FEDERATED_IDENTITY_KEY));
    }

    private <T> T getEnvAttribute(String key) {
        ApiProxy.Environment environment = ApiProxy.getCurrentEnvironment();
        Map<String, Object> attributes = environment.getAttributes();
        //noinspection unchecked
        return (T) attributes.get(key);
    }
}
