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

package org.jboss.test.capedwarf.images.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class IntegrationTest extends TestBase {

    private static final String TEST_IMAGE_RESOURCE = "capedwarf.png";

    @Deployment
    public static Archive getDeployment() {
        final WebArchive war = getCapedwarfDeployment();
        war.addClass(AbstractImagesServiceTestBase.class);
        war.addAsResource(TEST_IMAGE_RESOURCE);
        return war;
    }

    @Test
    public void testMakeImage() {
        Image image = loadTestImage();
        assertNotNull(image);
    }

    private Image loadTestImage() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEST_IMAGE_RESOURCE);
            byte[] byteArray = toBytes(inputStream, 0, Long.MAX_VALUE, true);
            return ImagesServiceFactory.makeImage(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHorizontalFlip() {
        Image image = loadTestImage();
        Transform horizontalFlip = ImagesServiceFactory.makeHorizontalFlip();

        Image flippedImage = ImagesServiceFactory.getImagesService().applyTransform(horizontalFlip, image);

        assertNotNull(flippedImage);
    }


    @Test
    public void asyncTransformRendersSameImageAsNonAsyncTransform() throws ExecutionException, InterruptedException {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();

        if (isJBossImpl(imagesService) == false)
            return;

        Transform transform = ImagesServiceFactory.makeHorizontalFlip();
        Image synchronouslyTransformedImage = imagesService.applyTransform(transform, loadTestImage());
        Future<Image> future = imagesService.applyTransformAsync(transform, loadTestImage());
        Image asynchronouslyTransformedImage = future.get();

        AbstractImagesServiceTestBase.assertImagesEqual(synchronouslyTransformedImage, asynchronouslyTransformedImage);
    }

    protected static byte[] toBytes(InputStream is, long start, long end, boolean closeStream) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1 && end > 0) {
                if (start > 0)
                    continue;

                baos.write(b);
                start--;
                end--;
            }
            return baos.toByteArray();
        } finally {
            if (closeStream)
                try {
                    is.close();
                } catch (IOException ignored) {
                }
        }
    }
}
