/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * Abstract http servlet request.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractHttpServletRequest extends AbstractServletRequest implements HttpServletRequest {

    private String path;
    private String method;
    private List<Cookie> cookies = new ArrayList<Cookie>();
    private Map<String, Set<String>> headers = new HashMap<String, Set<String>>();
    private Map<String, Part> parts = new HashMap<String, Part>();
    private HttpSession session;

    protected AbstractHttpServletRequest(ServletContext context) {
        super(context);
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void addHeader(String name, String value) {
        Set<String> set = headers.get(name);
        if (set == null) {
            set = new HashSet<String>();
            headers.put(name, set);
        }
        set.add(value);
    }

    public void addHeaders(String name, String[] values) {
        Set<String> set = headers.get(name);
        if (set == null) {
            set = new HashSet<String>();
            headers.put(name, set);
        }
        set.addAll(Arrays.asList(values));
    }

    public void addHeaders(Map<String, Set<String>> map) {
        headers.putAll(map);
    }

    public void addPart(String name, Part part) {
        parts.put(name, part);
    }

    public String getAuthType() {
        return null;  // TODO
    }

    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    public long getDateHeader(String name) {
        final String header = getHeader(name);
        return header != null ? Long.parseLong(header) : -1;
    }

    public String getHeader(String name) {
        final Set<String> h = headers.get(name);
        return (h != null && h.isEmpty() == false) ? h.iterator().next() : null;
    }

    public Enumeration<String> getHeaders(String name) {
        final Set<String> h = headers.get(name);
        return (h != null && h.isEmpty() == false) ? Collections.enumeration(h) : Collections.enumeration(Collections.<String>emptySet());
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public int getIntHeader(String name) {
        final String header = getHeader(name);
        return header != null ? Integer.parseInt(header) : -1;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathInfo() {
        return null;  // TODO
    }

    public String getPathTranslated() {
        return null;  // TODO
    }

    public String getContextPath() {
        return getServletContext().getContextPath();
    }

    public String getQueryString() {
        return null;  // TODO
    }

    public String getRemoteUser() {
        return null;  // TODO
    }

    public boolean isUserInRole(String role) {
        return true;  // TODO
    }

    public Principal getUserPrincipal() {
        return null;  // TODO
    }

    public String getRequestedSessionId() {
        return null;  // TODO
    }

    public String getRequestURI() {
        return path; // OK?
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(path); // OK?
    }

    public String getServletPath() {
        return path;
    }

    public void setServletPath(String path) {
        this.path = path;
    }

    public HttpSession getSession(boolean create) {
        if (session == null && create) {
            session = new MockHttpSession(getServletContext());
        }
        return session;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public boolean isRequestedSessionIdValid() {
        return false;  // TODO
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;  // TODO
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;  // TODO
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;  // TODO
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;  // TODO
    }

    public void login(String username, String password) throws ServletException {
        // TODO
    }

    public void logout() throws ServletException {
        // TODO
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return parts.values();
    }

    public Part getPart(String name) throws IOException, ServletException {
        return parts.get(name);
    }

    public String changeSessionId() {
        return null;
    }

    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    public long getContentLengthLong() {
        return (long) getContentLength();
    }
}
