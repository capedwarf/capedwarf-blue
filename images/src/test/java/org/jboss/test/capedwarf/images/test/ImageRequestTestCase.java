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

package org.jboss.test.capedwarf.images.test;

import com.google.appengine.api.blobstore.BlobKey;
import org.jboss.capedwarf.images.ImageRequest;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(All.class)
public class ImageRequestTestCase {

    @Test
    public void blobKeyOnly() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertFalse(request.isTransformationRequested());
    }

    @Test
    public void blobKeyAndImageSize() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/=s32");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertTrue(request.isTransformationRequested());
        assertEquals(32, request.getImageSize());
        assertFalse(request.isCrop());
    }

    @Test
    public void blobKeyAndImageSizeAndCrop() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/=s32-c");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertTrue(request.isTransformationRequested());
        assertEquals(32, request.getImageSize());
        assertTrue(request.isCrop());
    }


}
