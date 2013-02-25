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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.jboss.capedwarf.appidentity.CapedwarfHttpServletRequestWrapper;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfAuthenticator extends AuthenticatorBase {
    protected boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        HttpSession session = request.getSession();
        if (session == null)
            return false;

        CapedwarfUserPrincipal principal = getPrincipal(session);
        if (principal != null) {
            request.setUserPrincipal(principal);
            return true;
        } else {
            return false;
        }
    }

    protected CapedwarfUserPrincipal getPrincipal(HttpSession session) {
        return (CapedwarfUserPrincipal) session.getAttribute(CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY);
    }
}
