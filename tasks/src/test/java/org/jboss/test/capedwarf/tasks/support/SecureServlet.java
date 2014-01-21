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

package org.jboss.test.capedwarf.tasks.support;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SecureServlet extends HttpServlet {
    private Logger log = Logger.getLogger(SecureServlet.class.getName());

    private static ServletRequest lastRequest;

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        log.info("Secure - " + req);
        lastRequest = req;
    }

    public static ServletRequest getLastRequest() {
        return lastRequest;
    }

    public static void reset() {
        lastRequest = null;
    }
}
