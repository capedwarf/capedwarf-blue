/*
 *
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
 *
 */

package org.jboss.capedwarf.appidentity;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import org.jboss.capedwarf.blobstore.ExposedBlobstoreService;
import org.jboss.capedwarf.common.servlet.ServletUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GAEFilter implements Filter {

    private ServletContext servletContext;

    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;

        if (StaticServlet.isStaticFile(httpRequest) && fileExists(httpRequest)) {
            serveStaticFile((HttpServletRequest) req, (HttpServletResponse) res);
            return;
        }

        CapedwarfHttpServletRequestWrapper request = new CapedwarfHttpServletRequestWrapper(httpRequest);
        CapedwarfHttpServletResponseWrapper response = new CapedwarfHttpServletResponseWrapper(httpResponse);
        request.setAttribute(CapedwarfHttpServletResponseWrapper.class.getName(), response);
        chain.doFilter(request, response);

        serveBlobIfNecessary(response);
    }

    private boolean fileExists(HttpServletRequest request) throws MalformedURLException {
        String uri = ServletUtils.getRequestURIWithoutContextPath(request);
        return servletContext.getResource(uri) != null;
    }

    private void serveStaticFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
        dispatcher.forward(request, response);
    }

    private void serveBlobIfNecessary(CapedwarfHttpServletResponseWrapper response) throws IOException {
        String blobKey = response.getBlobKey();
        if (blobKey != null) {
            ExposedBlobstoreService blobstoreService = (ExposedBlobstoreService) BlobstoreServiceFactory.getBlobstoreService();
            blobstoreService.serveBlob(new BlobKey(blobKey), response.getBlobRange(), response);
        }
    }

    public void destroy() {
    }
}
