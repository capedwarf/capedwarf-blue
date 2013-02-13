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
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.images.ImageServlet;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class ImageServletTest {

    private static final String BLOB_KEY_STRING = "key123";
    private static final BlobKey BLOB_KEY = new BlobKey(BLOB_KEY_STRING);

    @Before
    public void setUp() {
        CapedwarfEnvironment.createThreadLocalInstance();
    }

    @After
    public void tearDown() {
        CapedwarfEnvironment.clearThreadLocalInstance();
    }

    @Test
    public void servingUrlContainsBlobKey() throws Exception {
        String url = ImageServlet.getServingUrl(BLOB_KEY);
        assertTrue(url.contains(BLOB_KEY_STRING));
    }

    @Test
    public void servingUrlWithImageSize() throws Exception {
        String baseUrl = ImageServlet.getServingUrl(BLOB_KEY);

        String actualUrl = ImageServlet.getServingUrl(BLOB_KEY, 32, false);
        String expectedUrl = baseUrl + "=s32";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void servingUrlWithImageSizeAndCrop() throws Exception {
        String baseUrl = ImageServlet.getServingUrl(BLOB_KEY);

        String actualUrl = ImageServlet.getServingUrl(BLOB_KEY, 32, true);
        String expectedUrl = baseUrl + "=s32-c";
        assertEquals(expectedUrl, actualUrl);
    }

}
