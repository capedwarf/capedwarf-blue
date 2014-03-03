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

import java.util.Map;

import javax.servlet.ServletContext;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.LoginConfig;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class AuthenticatorServletExtension implements ServletExtension {
    private static final String CAPEDWARF = "CAPEDWARF";

    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        final LoginConfig loginConfig = deploymentInfo.getLoginConfig();
        if (loginConfig == null || ServletUtils.CAPEDWARF_REALM.equals(loginConfig.getRealmName())) {
            return; // looks like there is no explicit secure resources / content
        }

        final String tgt = servletContext.getInitParameter("__TGT__");
        AuthenticationMechanismFactory amf;
        if (CAPEDWARF.equals(tgt)) {
            amf = new CapedwarfUsersAuthenticatorFactory();
        } else {
            amf = new CapedwarfBasicAuthenticatorFactory();
        }
        deploymentInfo.addAuthenticationMechanism(CAPEDWARF, amf);
    }

    private static class CapedwarfUsersAuthenticatorFactory implements AuthenticationMechanismFactory {
        public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory, Map<String, String> properties) {
            return new CapedwarfUsersAuthenticator();
        }
    }

    private static class CapedwarfBasicAuthenticatorFactory implements AuthenticationMechanismFactory {
        public AuthenticationMechanism create(String mechanismName, FormParserFactory formParserFactory, Map<String, String> properties) {
            return new CapedwarfBasicAuthenticator();
        }
    }
}
