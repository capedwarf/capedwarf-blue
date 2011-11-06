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

package org.jboss.capedwarf.images;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.capedwarf.common.config.JBossEnvironment;
import org.jboss.capedwarf.common.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@WebServlet(urlPatterns = ImageServlet.SERVLET_URI)
public class ImageServlet extends HttpServlet {
    public static final String SERVLET_URI = "/_capedwarf_/image";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        ImageRequest imageRequest = new ImageRequest(req.getPathInfo());
        if (imageRequest.isTransformationRequested()) {
            serveTransformedImage(imageRequest, response);
        } else {
            serveUntransformedImage(imageRequest, response);
        }
    }

    private void serveUntransformedImage(ImageRequest imageRequest, HttpServletResponse response) throws IOException {
        BlobstoreServiceFactory.getBlobstoreService().serve(imageRequest.getBlobKey(), response);
    }

    private void serveTransformedImage(ImageRequest imageRequest, HttpServletResponse response) throws IOException {
        Image image = loadImage(imageRequest.getBlobKey());
        Image transformedImage = transform(image, imageRequest.getImageSize(), imageRequest.isCrop());
        serve(transformedImage, response);
    }

    private Image loadImage(BlobKey blobKey) {
        return ImagesServiceFactory.makeImageFromBlob(blobKey);
    }

    private Image transform(Image image, int imageSize, boolean crop) {
        Transform transform = makeTransform(imageSize, crop);
        return ImagesServiceFactory.getImagesService().applyTransform(transform, image);
    }

    private void serve(Image image, HttpServletResponse response) throws IOException {
        InputStream in = new ByteArrayInputStream(image.getImageData());
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            IOUtils.copyStream(in, response.getOutputStream());
        } finally {
            in.close();
        }
    }

    private Transform makeTransform(int imageSize, boolean crop) {
        if (crop)
            return ImagesServiceFactory.makeCrop(0, 0, imageSize, imageSize);
        else
            return ImagesServiceFactory.makeResize(imageSize, imageSize);
    }

    public static String getServingUrl(BlobKey blobKey) {
        return getServletUrl() + "/" + blobKey.getKeyString() + "/";
    }

    public static String getServingUrl(BlobKey blobKey, int imageSize, boolean crop) {
        return getServingUrl(blobKey)
                + ImageRequest.SIZE_TOKEN + imageSize
                + (crop ? ImageRequest.CROP_TOKEN : "");
    }

    private static String getServletUrl() {
        return JBossEnvironment.getThreadLocalInstance().getBaseApplicationUrl() + SERVLET_URI;
    }

}
