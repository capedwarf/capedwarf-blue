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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import io.undertow.servlet.handlers.DefaultServlet;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.FilePattern;
import org.jboss.capedwarf.shared.config.StaticFileHttpHeader;
import org.jboss.capedwarf.shared.config.StaticFileInclude;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class StaticServlet extends DefaultServlet {

    public static final StaticFileInclude DEFAULT_INCLUDE = new StaticFileInclude("**", null);
    private static final long DEFAULT_EXPIRATION_SECONDS = 600; // 10 minutes

    static boolean doServeStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = ServletUtils.getRequestURIWithoutContextPath(request);
        ServletContext servletContext = request.getServletContext();
        URL resource = servletContext.getResource(path);

        boolean doServe = matchesStaticFilePath(path) && fileExists(resource) && !isJsp(servletContext, path) && !isDir(resource);
        if (doServe) {
            serveStaticFile(request, response);
        }
        return doServe;
    }

    private static boolean isJsp(ServletContext servletContext, String path) {
        for (String pattern : servletContext.getServletRegistrations().get("Default JSP Servlet").getMappings()) {
            if (matches(path, pattern)) {
                return true;
            }
        }
        JspConfigDescriptor jspConfigDescriptor = servletContext.getJspConfigDescriptor();
        if (jspConfigDescriptor != null) {
            for (JspPropertyGroupDescriptor groupDescriptor : jspConfigDescriptor.getJspPropertyGroups()) {
                for (String pattern : groupDescriptor.getUrlPatterns()) {
                    if (matches(path, pattern)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isDir(URL resource) throws MalformedURLException, ServletException {
        try {
            return Files.isDirectory(Paths.get(resource.toURI()));
        } catch (URISyntaxException e) {
            throw new ServletException(e);
        }
    }

    private static boolean matches(String path, String pattern) {
        return Pattern.compile(pattern.replaceAll("\\*", ".*")).matcher(path).matches();
    }

    private static boolean matchesStaticFilePath(String path) {
        return getMatchedStaticFileInclude(path) != null;
    }

    private static StaticFileInclude getMatchedStaticFileInclude(String path) {
        final ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
        if (appConfig == null) {
            return null; // handle undeploy
        }

        AppEngineWebXml appEngineWebXml = appConfig.getAppEngineWebXml();
        if (!matches(path, appEngineWebXml.getStaticFileExcludes())) {
            List<StaticFileInclude> includes = appEngineWebXml.getStaticFileIncludes();
            if (includes.isEmpty()) {
                return DEFAULT_INCLUDE;
            } else {
                return getMatchedFilePattern(path, includes);
            }
        }
        return null;
    }

    private static boolean fileExists(URL resource) throws MalformedURLException {
        return (resource != null);
    }

    private static boolean matches(String uri, List<? extends FilePattern> patterns) {
        return getMatchedFilePattern(uri, patterns) != null;
    }

    private static <E extends FilePattern> E getMatchedFilePattern(String uri, List<E> patterns) {
        for (E pattern : patterns) {
            if (pattern.matches(uri)) {
                return pattern;
            }
        }
        return null;
    }

    private static void serveStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
        dispatcher.forward(request, response);
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String path = ServletUtils.getRequestURIWithoutContextPath(req);
        StaticFileInclude include = getMatchedStaticFileInclude(path);
        if (include != null) {
            HttpServletRequest delegate = new HttpServletRequestWrapper(req) {
                @Override
                public String getPathInfo() {
                    return getPublicRoot() + path; // return full file path
                }
            };
            for (StaticFileHttpHeader header : include.getHeaders()) {
                resp.addHeader(header.getHeaderName(), header.getHeaderValue());
            }
            addCacheHeaders(resp, include);
            super.service(delegate, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getPublicRoot() {
        final ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
        if (appConfig == null) {
            return ""; // handle undeploy
        }

        String publicRoot = appConfig.getAppEngineWebXml().getPublicRoot();
        return publicRoot == null ? "" : publicRoot;
    }

    private void addCacheHeaders(HttpServletResponse resp, StaticFileInclude include) {
        long now = System.currentTimeMillis();
        long expirationSeconds = include.getExpirationSeconds() == null ? DEFAULT_EXPIRATION_SECONDS : include.getExpirationSeconds();
        resp.setDateHeader("Date", now);
        resp.setDateHeader("Expires", now + expirationSeconds * 1000);
        resp.setHeader("Cache-Control", "public, max-age=" + expirationSeconds);
    }
}
