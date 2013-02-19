/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import org.jboss.capedwarf.images.util.ColorUtils;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.images.support.ImageUtils;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ImagesServiceTestBase extends TestBase {

    protected static final String CAPEDWARF_BMP = "capedwarf.bmp";
    protected static final String CAPEDWARF_GIF = "capedwarf.gif";
    protected static final String CAPEDWARF_JPG = "capedwarf.jpg";
    protected static final String CAPEDWARF_PNG = "capedwarf.png";
    protected static final String CAPEDWARF_TIF = "capedwarf.tif";

    protected ImagesService imagesService;

    @Before
    public void setUp() throws Exception {
        imagesService = ImagesServiceFactory.getImagesService();
    }

    protected int[] getARGBPixel(Image image, int x, int y) {
        BufferedImage bufferedImage = getBufferedImage(image);
        int rgb = bufferedImage.getRGB(x, y);
        return ColorUtils.toIntArray((long) rgb);
    }

    protected static WritableRaster getRaster(Image image) {
        BufferedImage bufferedImage = getBufferedImage(image);
        return bufferedImage.getRaster();
    }

    protected static BufferedImage getBufferedImage(Image image) {
        return convertToBufferedImage(image.getImageData());
    }

    protected Image createImageFrom(BufferedImage bufferedImage) {
        return ImagesServiceFactory.makeImage(getByteArray(bufferedImage, "PNG"));
    }

    protected static void assertImagesEqual(Image image1, Image image2) {
        assertEquals(image1.getWidth(), image2.getWidth());
        assertEquals(image1.getHeight(), image2.getHeight());
        ImageUtils.assertRasterEquals(getRaster(image1), getRaster(image2));
    }

    protected Image createTestImage() {
        BufferedImage bufferedImage = createTestBufferedImage();
        return createImageFrom(bufferedImage);
    }

    protected BufferedImage createTestBufferedImage() {
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

    // copied from ImageUtils

    protected static byte[] getByteArray(RenderedImage image, String formatName) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, formatName, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static BufferedImage convertToBufferedImage(byte[] byteArray) {
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static int[][] histogram(BufferedImage bufferedImage) {
        int[][] histogram = new int[3][256];
        WritableRaster raster = bufferedImage.getRaster();
        int pixel[] = new int[4];
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                raster.getPixel(x, y, pixel);
                for (int i = 0; i < 3; i++) {
                    histogram[i][pixel[i]]++;
                }
            }
        }
        return histogram;
    }

    protected Image loadTestImage() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CAPEDWARF_PNG);
            byte[] byteArray = toBytes(inputStream, 0, Long.MAX_VALUE, true);
            return ImagesServiceFactory.makeImage(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
