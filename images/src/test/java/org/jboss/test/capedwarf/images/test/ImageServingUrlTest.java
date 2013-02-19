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

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@SuppressWarnings("deprecation")
@RunWith(Arquillian.class)
@Category(All.class)
public class ImageServingUrlTest extends ImagesServiceTestBase {

    private BlobKey blobKey;
    private FileService fileService;

    @Deployment
    public static Archive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(ImagesServiceTestBase.class);
        war.addClass(IOUtils.class);
        war.addAsResource(CAPEDWARF_PNG);
        return war;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("image/png");
        FileWriteChannel channel = fileService.openWriteChannel(file, true);
        try {
            ReadableByteChannel in = Channels.newChannel(getCapedwarfPngInputStream());
            try {
                IOUtils.copy(in, channel);
            } finally {
                in.close();
            }
        } finally {
            channel.closeFinally();
        }

        blobKey = fileService.getBlobKey(file);
    }

    private InputStream getCapedwarfPngInputStream() {
        return ImageServingUrlTest.class.getResourceAsStream("/" + CAPEDWARF_PNG);
    }

    @After
    public void tearDown() throws Exception {
        try {
            fileService.delete(fileService.getBlobFile(blobKey));
        } catch (IOException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void servingUrlWithNonexistentBlobKeyThrowsException() throws Exception {
        imagesService.getServingUrl(new BlobKey("nonexistentBlob"));
    }

    @Test
    public void servingUrlWithImageSize() throws Exception {
        String baseUrl = imagesService.getServingUrl(blobKey);
        String actualUrl = imagesService.getServingUrl(blobKey, 32, false);
        String expectedUrl = baseUrl + "=s32";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void servingUrlWithImageSizeAndCrop() throws Exception {
        String baseUrl = imagesService.getServingUrl(blobKey);
        String actualUrl = imagesService.getServingUrl(blobKey, 32, true);
        String expectedUrl = baseUrl + "=s32-c";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void servingUrlWithSecureFlag() throws Exception {
        String url = imagesService.getServingUrl(blobKey, false);
        assertStartsWith("http://", url);

        url = imagesService.getServingUrl(blobKey, 32, false, false);
        assertStartsWith("http://", url);

        if (!isRunningInsideGaeDevServer()) {
            url = imagesService.getServingUrl(blobKey, true);
            assertStartsWith("https://", url);

            url = imagesService.getServingUrl(blobKey, 32, false, true);
            assertStartsWith("https://", url);
        }
    }

    private void assertStartsWith(String prefix, String url) {
        assertTrue("Expected string to start with \"" + prefix + "\", but was: " + url, url.startsWith(prefix));
    }

}
