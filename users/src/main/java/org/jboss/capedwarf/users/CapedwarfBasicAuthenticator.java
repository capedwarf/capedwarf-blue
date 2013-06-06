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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfBasicAuthenticator extends AbstractAuthenticator {
    private volatile AuthenticatorBase basicDelegate;

    protected boolean authenticateAdmin(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        try {
            return getBasicDelegate().authenticate(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    protected boolean isAdminConsoleAccess(Request request, LoginConfig config) {
        final String authMethod = config.getAuthMethod();
        // enabled admin console sets BASIC auth method
        return (authMethod != null && "BASIC".equals(authMethod.toUpperCase()) && isAdminConsoleURI(request));
    }

    protected AuthenticatorBase getBasicDelegate() {
        if (basicDelegate == null) {
            synchronized (this) {
                if (basicDelegate == null) {
                    AuthenticatorBase tmp = new BasicAuthenticator();
                    tmp.setContainer(getContainer());
                    basicDelegate = tmp;
                }
            }
        }
        return basicDelegate;
    }
}
