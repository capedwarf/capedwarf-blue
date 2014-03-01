/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.admin;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.common.servlet.ServletUtils;

/**
 * @author Marko Luksa
 * @author Ales Justin
 */
public class AdminServlet extends HttpServlet {
    private VelocityEngine velocity;

    @Inject BeanManager manager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        createVelocityEngine(config.getServletContext());
    }

    @SuppressWarnings("unchecked")
    private void createVelocityEngine(ServletContext context) throws ServletException {
        try {
            velocity = VelocityUtils.create(context);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (isVelocityPageRequest(req)) {
                serveVelocityPage(req, resp);
            } else {
                serveResource(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void serveResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/org/jboss/capedwarf/admin" + req.getPathInfo())) {
            IOUtils.copyStream(in, resp.getOutputStream());
        }
    }

    private boolean isVelocityPageRequest(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.endsWith(".vm");
    }

    private void serveVelocityPage(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Context context = CapedwarfVelocityContext.createThreadLocalInstance(manager, req);
        try {
            ServletUtils.handleResponse(resp, "iso-8859-1", null); // same as in header.vm

            Template template = velocity.getTemplate(getTemplatePath(req));
            template.merge(context, resp.getWriter());
        } finally {
            CapedwarfVelocityContext.clearThreadLocalInstance();
        }
    }

    private String getTemplatePath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/index.vm";
        } else if (pathInfo.endsWith("/")) {
            pathInfo += "index.vm";
        }
        return "/org/jboss/capedwarf/admin" + pathInfo;
    }

}
