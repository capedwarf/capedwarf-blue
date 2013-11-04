/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.handlers.ServletRequestContext;
import org.jboss.capedwarf.appidentity.CapedwarfHttpServletRequestWrapper;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractAuthenticator implements AuthenticationMechanism {
    protected static final String KEY = CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY;

    private final String supportedAuthMethod;

    protected AbstractAuthenticator(String supportedAuthMethod) {
        this.supportedAuthMethod = supportedAuthMethod;
    }

    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        final ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        final HttpServletRequest request = (HttpServletRequest) servletRequestContext.getServletRequest();
        final HttpServletResponse response = (HttpServletResponse) servletRequestContext.getServletResponse();
        final LoginConfig config = servletRequestContext.getDeployment().getDeploymentInfo().getLoginConfig();

        if (isAdminConsoleAccess(request, config)) {
            return authenticateAdmin(request, response, config);
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }

        CapedwarfUserPrincipal principal = getPrincipal(session);
        if (principal != null) {
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        } else {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }
    }

    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        return new ChallengeResult(false);
    }

    protected boolean isAdminConsoleAccess(HttpServletRequest request, LoginConfig config) {
        final String authMethod = (config != null ? config.getAuthMethod() : null);
        // enabled admin console sets OAUTH auth method
        return (authMethod != null && supportedAuthMethod.equals(authMethod.toUpperCase()) && isAdminConsoleURI(request));
    }

    protected abstract AuthenticationMechanismOutcome authenticateAdmin(HttpServletRequest request, HttpServletResponse response, LoginConfig config);

    protected boolean isAdminConsoleURI(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // any better way?
        return (requestURI != null && requestURI.contains("_ah/admin"));
    }

    protected AuthenticationMechanismOutcome unauthorized(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
    }

    protected CapedwarfUserPrincipal getPrincipal(HttpSession session) {
        return (CapedwarfUserPrincipal) session.getAttribute(KEY);
    }
}
