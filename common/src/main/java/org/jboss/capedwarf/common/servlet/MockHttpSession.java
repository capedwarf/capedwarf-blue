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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Mock / dummy http session.
 *
 * @author Ales Justin
 */
class MockHttpSession implements HttpSession {
    private final ServletContext servletContext;

    private Map<String, Object> attributes = new HashMap<>();
    private long creationTime = System.currentTimeMillis();
    private String id = UUID.randomUUID().toString();

    public MockHttpSession(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getId() {
        return id;
    }

    public void invalidate() {
        attributes.clear();
    }

    public boolean isNew() {
        return true;
    }

    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }

    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }

    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException();
    }

    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }

    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }
}
