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

package org.jboss.capedwarf.blobstore;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.UploadOptions;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.url.URLUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@WebServlet(urlPatterns = UploadServlet.URI)
@MultipartConfig()  // TODO
public class UploadServlet extends HttpServlet {

    public static final String URI = "/_capedwarf_/blobstore/upload";
    private static final String SUCCESS_PATH_PARAM = "successPath";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CapedwarfBlobstoreService blobstoreService = new CapedwarfBlobstoreService();
        blobstoreService.storeUploadedBlobs(request);
        response.sendRedirect(getSuccessPath(request));
    }

    private String getSuccessPath(HttpServletRequest request) {
        return request.getParameter(SUCCESS_PATH_PARAM);
    }

    public static String createUploadUrl(String successPath, UploadOptions uploadOptions) {
        return getServletUrl() + "?" + SUCCESS_PATH_PARAM + "=" + URLUtils.encode(successPath);   // TODO enforce uploadOptions
    }

    private static String getServletUrl() {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl() + URI;
    }

}
