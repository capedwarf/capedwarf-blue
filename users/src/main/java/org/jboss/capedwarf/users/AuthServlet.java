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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.google.appengine.api.utils.SystemProperty;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Production;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class AuthServlet extends HttpServlet {
    public static final String FEDERATED_IDENTITY_PARAM = "federatedIdentity";
    public static final String DESTINATION_URL_PARAM = "continue";
    public static final String AUTH_DOMAIN_PARAM = "authDomain";
    public static final String OTHER = "other";

    protected AuthHandler authHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        SystemProperty.Environment.Value environment = SystemProperty.environment.value();
        if (environment == Production) {
            authHandler = new ProductionAuthHandler();
        } else if (environment == Development) {
            authHandler = new DevelopmentAuthHandler();
        } else {
            throw new IllegalStateException("Unknown environment: " + environment);
        }
    }

    protected static String getServletUrl() {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl();
    }

}
