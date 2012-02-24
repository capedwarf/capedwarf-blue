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

package org.jboss.test.capedwarf.images;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import org.jboss.capedwarf.images.JBossImagesService;
import org.jboss.capedwarf.images.util.ColorUtils;
import org.jboss.capedwarf.images.util.ImageUtils;
import org.junit.Before;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossImagesServiceTest {

    protected ImagesService imagesService;

    @Before
    public void setUp() {
        imagesService = new JBossImagesService();
    }

    protected int[] getARGBPixel(Image image, int x, int y) {
        BufferedImage bufferedImage = getBufferedImage(image);
        int rgb = bufferedImage.getRGB(x, y);
        return ColorUtils.toIntArray((long) rgb);
    }

    protected int[] getPixel(Raster raster, int x, int y) {
        return raster.getPixel(x, y, (int[]) null);
    }

    protected WritableRaster getRaster(Image image) {
        BufferedImage bufferedImage = getBufferedImage(image);
        return bufferedImage.getRaster();
    }

    private BufferedImage getBufferedImage(Image image) {
        return ImageUtils.convertToBufferedImage(image.getImageData());
    }

    protected Image createImageFrom(BufferedImage bufferedImage) {
        return ImagesServiceFactory.makeImage(ImageUtils.getByteArray(bufferedImage, "PNG"));
    }

    protected void assertImagesEqual(Image image1, Image image2) {
        assertEquals(image1.getWidth(), image2.getWidth());
        assertEquals(image1.getHeight(), image2.getHeight());

        Raster raster1 = getRaster(image1);
        Raster raster2 = getRaster(image2);
        for (int y = 0; y < raster1.getHeight(); y++) {
            for (int x = 0; x < raster1.getWidth(); x++) {
                assertPixelsEqual(raster1, x, y, raster2, x, y);
            }
        }
    }

    protected void assertPixelEqual(int[] expectedPixel, int[] actualPixel) {
        if (!arraysEqual(expectedPixel, actualPixel)) {
            fail("Expected pixel " + formatPixel(expectedPixel) + "; was: " + formatPixel(actualPixel));
        }
    }

    protected void assertPixelsEqual(Raster raster, int x1, int y1, Raster transformedRaster, int x2, int y2) {
        int pixel1[] = getPixel(raster, x1, y1);
        int pixel2[] = getPixel(transformedRaster, x2, y2);

        if (!arraysEqual(pixel1, pixel2)) {
            fail("Original image's pixel at (" + x1 + "," + y1 + ") doesn't match transformed image's pixel at (" + x2 + "," + y2 + "). Original: " + formatPixel(pixel1) + "; transformed: " + formatPixel(pixel2));
        }
    }

    private String formatPixel(int[] pixel) {
        StringBuilder sbuf = new StringBuilder();
        for (int p : pixel) {
            sbuf.append(",").append(p);
        }
        return sbuf.substring(1);
    }

    private boolean arraysEqual(int[] array1, int[] array2) {
        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        return true;
    }

    protected Image createTestImage() {
        BufferedImage bufferedImage = createTestBufferedImage();
        return createImageFrom(bufferedImage);
    }

    private BufferedImage createTestBufferedImage() {
        BufferedImage image = new BufferedImage(10, 5, BufferedImage.TYPE_INT_ARGB);
        int color = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = new Color(color++, color++, color++, 255).getRGB();
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }
}
