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

package org.jboss.capedwarf.appidentity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import io.undertow.servlet.handlers.DefaultServlet;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.FilePattern;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class StaticServlet extends DefaultServlet {
    static boolean doServeStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        boolean doServe = matchesStaticFilePath(request) && fileExists(request);
        if (doServe) {
            serveStaticFile(request, response);
        }
        return doServe;
    }

    private static boolean matchesStaticFilePath(HttpServletRequest request) {
        final ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
        if (appConfig == null) {
            return false; // handle undeploy
        }

        AppEngineWebXml appEngineWebXml = appConfig.getAppEngineWebXml();
        String path = ServletUtils.getRequestURIWithoutContextPath(request);
        return matches(path, appEngineWebXml.getStaticFileIncludes()) && !matches(path, appEngineWebXml.getStaticFileExcludes());
    }

    private static boolean fileExists(HttpServletRequest request) throws MalformedURLException {
        String uri = ServletUtils.getRequestURIWithoutContextPath(request);
        ServletContext servletContext = request.getServletContext();
        URL resource = servletContext.getResource(uri);
        return (resource != null);
    }

    private static boolean matches(String uri, List<? extends FilePattern> patterns) {
        for (FilePattern pattern : patterns) {
            if (pattern.matches(uri)) {
                return true;
            }
        }
        return false;
    }

    private static void serveStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
        dispatcher.forward(request, response);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (matchesStaticFilePath(req)) {
            HttpServletRequest delegate = new HttpServletRequestWrapper(req) {
                @Override
                public String getPathInfo() {
                    return ServletUtils.getRequestURIWithoutContextPath(req); // return full file path
                }
            };
            super.service(delegate, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
