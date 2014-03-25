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

package org.jboss.test.capedwarf.images.support;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import org.jboss.capedwarf.common.io.IOUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class UrlServingServlet extends HttpServlet {


    private BlobKey blobKey;
    private ImagesService imagesService = ImagesServiceFactory.getImagesService();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            FileService fileService = FileServiceFactory.getFileService();
            AppEngineFile file = fileService.createNewBlobFile("image/png");
            FileWriteChannel channel = fileService.openWriteChannel(file, true);
            try {
                ReadableByteChannel in = Channels.newChannel(UrlServingServlet.class.getResourceAsStream("/capedwarf.png"));
                try {
                    IOUtils.copy(in, channel);
                } finally {
                    in.close();
                }
            } finally {
                channel.closeFinally();
            }

            blobKey = fileService.getBlobKey(file);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().endsWith("getImageUrl")) {
            String number = req.getParameter("image");

            String url = null;
            ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
            if ("basic".equals(number)) {
                url = imagesService.getServingUrl(options);
            } else if ("resized".equals(number)) {
                url = imagesService.getServingUrl(options.imageSize(100));
            } else if ("cropped".equals(number)) {
                url = imagesService.getServingUrl(options.imageSize(100).crop(true));
            }
            resp.getWriter().print(url);
        } else if (req.getRequestURI().endsWith("deleteServingUrl")) {
            imagesService.deleteServingUrl(blobKey);
        } else {
            throw new RuntimeException("Don't know what to do");
        }
    }
}
