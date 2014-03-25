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

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ImageRequest {

    public static final String SIZE_TOKEN = "=s";
    public static final String CROP_TOKEN = "-c";

    private ImageId imageId;
    private Integer imageSize;
    private boolean crop;

    public ImageRequest(String pathInfo) {
        StringTokenizer tokenizer = new StringTokenizer(pathInfo, "/");
        imageId = new ImageId(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            parseSizeAndCrop(tokenizer.nextToken());
        }
    }

    private void parseSizeAndCrop(String str) {
        if (str.startsWith(SIZE_TOKEN)) {
            if (str.endsWith(CROP_TOKEN)) {
                crop = true;
                str = str.substring(0, str.length() - CROP_TOKEN.length());
            }

            imageSize = Integer.parseInt(str.substring(SIZE_TOKEN.length()));
        }
    }

    public ImageId getImageId() {
        return imageId;
    }

    public boolean isTransformationRequested() {
        return imageSize != null;
    }

    public int getImageSize() {
        return imageSize;
    }

    public boolean isCrop() {
        return crop;
    }
}
