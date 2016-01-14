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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;
import org.jboss.capedwarf.common.servlet.ServletUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfUsersAuthenticator extends AbstractAuthenticator {
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        final ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        final HttpServletRequest request = (HttpServletRequest) servletRequestContext.getServletRequest();
        final HttpServletResponse response = (HttpServletResponse) servletRequestContext.getServletResponse();

        String path = ServletUtils.getRequestURIWithoutContextPath(request);
        if (path.startsWith(AuthServlet.LOGIN_PATH)) {
            RequestDispatcher disp = request.getRequestDispatcher(path);
            try {
                disp.forward(request, response);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            try {
                UserService userService = UserServiceFactory.getUserService();
                String location = userService.createLoginURL(request.getRequestURI());

                ServletUtils.forward(request, response, location);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return new ChallengeResult(true);
    }
}
