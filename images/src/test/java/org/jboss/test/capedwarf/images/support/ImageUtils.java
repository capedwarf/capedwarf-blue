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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class ImageUtils {

    public static int[] getPixel(Raster raster, int x, int y) {
        return raster.getPixel(x, y, (int[]) null);
    }

    public static void assertImagesEqual(BufferedImage image1, BufferedImage image2) {
        assertRasterEquals(image1.getRaster(), image2.getRaster());
    }

    public static void assertRasterEquals(Raster raster1, Raster raster2) {
        assertEquals("image widths don't match", raster1.getWidth(), raster2.getWidth());
        assertEquals("image heights don't match", raster1.getHeight(), raster2.getHeight());
        for (int y = 0; y < raster1.getHeight(); y++) {
            for (int x = 0; x < raster1.getWidth(); x++) {
                assertPixelsEqual(raster1, x, y, raster2, x, y);
            }
        }
    }

    public static void assertPixelsEqual(Raster raster, int x1, int y1, Raster transformedRaster, int x2, int y2) {
        int pixel1[] = getPixel(raster, x1, y1);
        int pixel2[] = getPixel(transformedRaster, x2, y2);

        if (!arraysEqual(pixel1, pixel2)) {
            fail("Original image's pixel at (" + x1 + "," + y1 + ") doesn't match transformed image's pixel at (" + x2 + "," + y2 + "). Original: " + formatPixel(pixel1) + "; transformed: " + formatPixel(pixel2));
        }
    }

    public static String formatPixel(int[] pixel) {
        StringBuilder sbuf = new StringBuilder();
        for (int p : pixel) {
            sbuf.append(",").append(p);
        }
        return sbuf.substring(1);
    }

    public static boolean arraysEqual(int[] array1, int[] array2) {
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

    public static void assertPixelEqual(int[] expectedPixel, int[] actualPixel) {
        if (!arraysEqual(expectedPixel, actualPixel)) {
            fail("Expected pixel " + formatPixel(expectedPixel) + "; was: " + formatPixel(actualPixel));
        }
    }

    public static void dumpImage(BufferedImage image, String filename) {
        try {
            if (!filename.endsWith(".png")) {
                filename += ".png";
            }
            ImageIO.write(image, "PNG", new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
