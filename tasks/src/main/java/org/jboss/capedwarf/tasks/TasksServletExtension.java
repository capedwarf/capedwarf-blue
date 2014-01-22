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

package org.jboss.capedwarf.tasks;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SingleConstraintMatch;
import io.undertow.servlet.handlers.ServletRequestContext;
import org.jboss.capedwarf.shared.servlet.Mock;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class TasksServletExtension implements ServletExtension {
    private static final String ROLES = "CAPEDWARF_ROLES";

    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        LoginConfig loginConfig = deploymentInfo.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = new LoginConfig("CAPEDWARF");
            deploymentInfo.setLoginConfig(loginConfig);
        }
        loginConfig.addLastAuthMethod(ROLES);
        deploymentInfo.addAuthenticationMechanism(ROLES, new RolesAuthenticatorFactory());
    }

    private static class RolesAuthenticatorFactory implements AuthenticationMechanismFactory {
        public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory, Map<String, String> properties) {
            return new RolesAuthenticator();
        }
    }

    private static class RolesAuthenticator implements AuthenticationMechanism {
        public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
            final ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
            final List<SingleConstraintMatch> constraints = servletRequestContext.getRequiredConstrains();
            if (constraints == null || constraints.isEmpty()) {
                return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
            }

            final HttpServletRequest request = (HttpServletRequest) servletRequestContext.getServletRequest();
            if (request instanceof Mock == false) {
                return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
            }

            final Set<String> requiredRoles = new HashSet<>();
            for (SingleConstraintMatch sc : constraints) {
                requiredRoles.addAll(sc.getRequiredRoles());
            }

            for (String role : requiredRoles) {
                if (request.isUserInRole(role) == false) {
                    return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
                }
            }

            securityContext.authenticationComplete(new RolesAccount(requiredRoles, request.getUserPrincipal()), ROLES, false);
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        }

        public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
            return new ChallengeResult(false);
        }
    }

    private static final class RolesAccount implements Account {
        private final Set<String> roles;
        private final Principal principal;

        private RolesAccount(Set<String> roles, Principal principal) {
            this.roles = roles;
            this.principal = principal;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public Set<String> getRoles() {
            return roles;
        }
    }
}
