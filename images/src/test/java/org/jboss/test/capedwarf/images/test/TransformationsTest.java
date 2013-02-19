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

import java.awt.image.Raster;
import java.util.Arrays;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class TransformationsTest extends ImagesServiceTestBase {

    @Deployment
    public static Archive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(ImagesServiceTestBase.class);
        war.addAsResource(CAPEDWARF_PNG);
        return war;
    }

    @Test
    public void testResize() {
        int resizedWidth = 300;
        int resizedHeight = 200;
        Image originalImage = createTestImage();
        Transform resize = ImagesServiceFactory.makeResize(resizedWidth, resizedHeight);

        Image resizedImage = imagesService.applyTransform(resize, originalImage);

        assertEquals(resizedWidth, resizedImage.getWidth());
        assertEquals(resizedHeight, resizedImage.getHeight());
    }

    @Test
    public void testCrop() {

        int xOffset = 4;
        int yOffset = 1;
        int croppedWidth = 5;
        int croppedHeight = 3;

        Image image = createTestImage();

        float leftX = (float) xOffset / image.getWidth();
        float topY = (float) yOffset / image.getHeight();
        float rightX = (float) (xOffset + croppedWidth) / image.getWidth();
        float bottomY = (float) (yOffset + croppedHeight) / image.getHeight();

        Transform crop = ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY);

        Image croppedImage = imagesService.applyTransform(crop, image);

        Raster raster = getRaster(image);
        Raster croppedRaster = getRaster(croppedImage);
        assertEquals(croppedWidth, croppedRaster.getWidth());
        assertEquals(croppedHeight, croppedRaster.getHeight());
        for (int y = 0; y < croppedHeight; y++) {
            for (int x = 0; x < croppedWidth; x++) {
                assertPixelsEqual(raster, xOffset + x, yOffset + y, croppedRaster, x, y);
            }
        }
    }

    @Test
    public void testHorizontalFlip() {
        Image image = createTestImage();
        Transform horizontalFlip = ImagesServiceFactory.makeHorizontalFlip();

        Image flippedImage = imagesService.applyTransform(horizontalFlip, image);

        Raster raster = getRaster(image);
        Raster flippedRaster = getRaster(flippedImage);
        assertEquals(raster.getWidth(), flippedRaster.getWidth());
        assertEquals(raster.getHeight(), flippedRaster.getHeight());
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                assertPixelsEqual(raster, x, y, flippedRaster, raster.getWidth() - x - 1, y);
            }
        }
    }

    @Test
    public void testVerticalFlip() {
        Image image = createTestImage();
        Transform verticalFlip = ImagesServiceFactory.makeVerticalFlip();

        Image flippedImage = imagesService.applyTransform(verticalFlip, image);

        Raster raster = getRaster(image);
        Raster flippedRaster = getRaster(flippedImage);
        assertEquals(raster.getWidth(), flippedRaster.getWidth());
        assertEquals(raster.getHeight(), flippedRaster.getHeight());
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                assertPixelsEqual(raster, x, y, flippedRaster, x, raster.getHeight() - y - 1);
            }
        }
    }

    @Test
    public void testRotate0Degrees() {
        Image image = createTestImage();
        Transform rotate0 = ImagesServiceFactory.makeRotate(0);
        Image rotatedImage = imagesService.applyTransform(rotate0, image);

        assertImagesEqual(image, rotatedImage);
    }

    @Test
    public void testRotate90Degrees() {
        Image image = createTestImage();
        Transform rotate90 = ImagesServiceFactory.makeRotate(90);
        Image rotatedImage = imagesService.applyTransform(rotate90, image);

        Raster raster = getRaster(image);
        Raster rotatedRaster = getRaster(rotatedImage);
        assertEquals(raster.getWidth(), rotatedRaster.getHeight());
        assertEquals(raster.getHeight(), rotatedRaster.getWidth());
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                assertPixelsEqual(raster, x, y, rotatedRaster, rotatedRaster.getWidth() - y - 1, x);
            }
        }
    }

    @Test
    public void testRotate180Degrees() {
        Image image = createTestImage();
        Transform rotate = ImagesServiceFactory.makeRotate(180);
        Image rotate180 = imagesService.applyTransform(rotate, image);

        Raster raster = getRaster(image);
        Raster rotatedRaster = getRaster(rotate180);
        assertEquals(raster.getWidth(), rotatedRaster.getWidth());
        assertEquals(raster.getHeight(), rotatedRaster.getHeight());
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                assertPixelsEqual(raster, x, y, rotatedRaster, rotatedRaster.getWidth() - x - 1, rotatedRaster.getHeight() - y - 1);
            }
        }
    }

    @Test
    public void testRotate270Degrees() {
        Image image = createTestImage();
        Transform rotate270 = ImagesServiceFactory.makeRotate(270);
        Image rotatedImage = imagesService.applyTransform(rotate270, image);

        Raster raster = getRaster(image);
        Raster rotatedRaster = getRaster(rotatedImage);
        assertEquals(raster.getWidth(), rotatedRaster.getHeight());
        assertEquals(raster.getHeight(), rotatedRaster.getWidth());
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                assertPixelsEqual(raster, x, y, rotatedRaster, y, rotatedRaster.getHeight() - x - 1);
            }
        }
    }

    @Test
    public void imFeelingLuckyReturnsTheSameImage() {
        Image image = createTestImage();
        Transform feelingLuckyTransform = ImagesServiceFactory.makeImFeelingLucky();
        Image improvedImage = imagesService.applyTransform(feelingLuckyTransform, image);

        assertEquals(image.getWidth(), improvedImage.getWidth());
        assertEquals(image.getHeight(), improvedImage.getHeight());
    }

    @Test
    public void compositeTransformRendersSameImageAsSeparateTransforms() {

        Transform transform1 = ImagesServiceFactory.makeHorizontalFlip();
        Transform transform2 = ImagesServiceFactory.makeVerticalFlip();
        Transform transform3 = ImagesServiceFactory.makeRotate(90);
        Transform compositeTransform = ImagesServiceFactory.makeCompositeTransform(Arrays.asList(transform1, transform2, transform3));

        Image separatelyTransformedImage = imagesService.applyTransform(transform1, createTestImage());
        separatelyTransformedImage = imagesService.applyTransform(transform2, separatelyTransformedImage);
        separatelyTransformedImage = imagesService.applyTransform(transform3, separatelyTransformedImage);

        Image imageTransformedWithComposite = imagesService.applyTransform(compositeTransform, createTestImage());

        assertImagesEqual(separatelyTransformedImage, imageTransformedWithComposite);
    }

}
