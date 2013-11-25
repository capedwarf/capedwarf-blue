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

package org.jboss.test.capedwarf.testsuite.servlet.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SimpleServlet extends HttpServlet {

    private void doSomethingWithReader(HttpServletRequest req, HttpServletResponse response) throws IOException {
        try (BufferedReader reader = new BufferedReader(req.getReader())) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            response.setContentType("application/json");
            System.err.println(String.format("Testing %s with content = '%s'", req.getMethod().toUpperCase(), out));
            response.getWriter().print(out.toString());
        }
    }

    private void doSomethingWithStream(HttpServletRequest req, HttpServletResponse response) throws IOException {
        try (InputStream is = req.getInputStream()) {
            StringBuilder out = new StringBuilder();
            int ch;
            while ((ch = is.read()) != -1) {
                out.append((char) ch);
            }
            response.setContentType("application/json");
            System.err.println(String.format("Testing %s with content = '%s'", req.getMethod().toUpperCase(), out));
            response.getWriter().print(out.toString());
        }
    }

    @Override
    public void doPut(HttpServletRequest req, final HttpServletResponse response) throws IOException {
        doSomethingWithReader(req, response);
    }

    @Override
    public void doPost(HttpServletRequest req, final HttpServletResponse response) throws IOException {
        doSomethingWithStream(req, response);
    }

}
