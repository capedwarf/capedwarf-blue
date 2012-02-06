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

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 *
 */
public class AdminServlet extends HttpServlet {

    private VelocityEngine velocity;

    @Inject BeanManager manager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        createVelocityEngine();
    }

    private void createVelocityEngine() throws ServletException {
        try {
            Properties props = new Properties();
            props.put("resource.loader", "class");
            props.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
            velocity = new VelocityEngine(props);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            serveVelocityPage(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void serveVelocityPage(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Context context = CapedwarfVelocityContext.createThreadLocalInstance(manager, req);

        Template template = velocity.getTemplate(getTemplatePath(req));
        OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
        template.merge(context, writer);
        writer.flush();

        CapedwarfVelocityContext.clearThreadLocalInstance();
    }

    private String getTemplatePath(HttpServletRequest req) {
        System.out.println("req.getPathInfo() = " + req.getPathInfo());
        return "/org/jboss/capedwarf/admin" + req.getPathInfo();
    }

}
