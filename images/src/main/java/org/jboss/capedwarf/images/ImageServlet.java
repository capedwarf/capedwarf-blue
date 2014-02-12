/*
 *
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
 *
 */

package org.jboss.capedwarf.images;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.io.IOUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@WebServlet(urlPatterns = ImageServlet.SERVLET_URI)
public class ImageServlet extends HttpServlet {
    public static final String SERVLET_URI = "/_ah/image";

    private BlobstoreService blobstoreService;

    @Override
    public void init() throws ServletException {
        super.init();
        blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    }

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
        byte[] imageData = blobstoreService.fetchData(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE - 1);
        return ImagesServiceFactory.makeImage(imageData);
    }

    private Image transform(Image image, int imageSize, boolean crop) {
        Transform transform = makeTransform(imageSize, crop);
        return ImagesServiceFactory.getImagesService().applyTransform(transform, image);
    }

    private void serve(Image image, HttpServletResponse response) throws IOException {
        try (InputStream in = new ByteArrayInputStream(image.getImageData())) {
            response.setStatus(HttpServletResponse.SC_OK);
            IOUtils.copyStream(in, response.getOutputStream());
        }
    }

    private Transform makeTransform(int imageSize, boolean crop) {
        if (crop)
            return ImagesServiceFactory.makeResize(imageSize, imageSize, 0.5f, 0.5f);
        else
            return ImagesServiceFactory.makeResize(imageSize, imageSize);
    }

    public static String getServingUrl(BlobKey blobKey, int imageSize, boolean crop, boolean secureUrl) {
        if (blobKey == null) {
            throw new IllegalArgumentException("Null blob key!");
        }

        StringBuilder builder = new StringBuilder(getServletUrl(secureUrl));
        builder.append("/");
        builder.append(blobKey.getKeyString());
        builder.append("/");
        if (imageSize > 0)
            builder.append(ImageRequest.SIZE_TOKEN).append(imageSize);
        if (crop)
            builder.append(ImageRequest.CROP_TOKEN);
        return builder.toString();
    }

    @SuppressWarnings("UnusedParameters")
    public static void deleteServingUrl(BlobKey blobKey) {
        // no-op atm
    }

    private static String getServletUrl(boolean secureUrl) {
        return CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl(secureUrl) + SERVLET_URI;
    }

}
