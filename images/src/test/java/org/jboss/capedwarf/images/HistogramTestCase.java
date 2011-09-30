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

package org.jboss.capedwarf.images;

import com.google.appengine.api.images.Image;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class HistogramTestCase extends JBossImagesServiceTestCase {

    private static final int[] WHITE_PIXEL = new int[]{255, 255, 255};

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    @Test
    public void histogramReturnsArrayOf3ArraysOfLength256() {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Image image = createImageFrom(bufferedImage);

        int[][] histogram = imagesService.histogram(image);

        assertEquals(3, histogram.length);
        assertEquals(256, histogram[0].length);
        assertEquals(256, histogram[1].length);
        assertEquals(256, histogram[2].length);
    }

    @Test
    public void histogramOfSingleWhitePixelImageReturnsCount1AtTone255() {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        bufferedImage.getRaster().setPixel(0, 0, WHITE_PIXEL);
        Image image = createImageFrom(bufferedImage);

        int[][] histogram = imagesService.histogram(image);

        assertEquals(1, histogram[RED][255]);
        assertEquals(1, histogram[GREEN][255]);
        assertEquals(1, histogram[BLUE][255]);
    }

    @Test
    public void histogramOfTwoWhitePixelsReturnsCount2AtTone255() {
        BufferedImage bufferedImage = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        bufferedImage.getRaster().setPixel(0, 0, WHITE_PIXEL);
        bufferedImage.getRaster().setPixel(1, 0, WHITE_PIXEL);
        Image image = createImageFrom(bufferedImage);

        int[][] histogram = imagesService.histogram(image);

        assertEquals(2, histogram[RED][255]);
        assertEquals(2, histogram[GREEN][255]);
        assertEquals(2, histogram[BLUE][255]);
    }

    @Test
    public void histogramOf256PixelImageWherePixelColorsRangeFrom0To255ReturnsCount1AtAllTones() {
        BufferedImage bufferedImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int tone = y * 16 + x;
                bufferedImage.getRaster().setPixel(x, y, new int[]{tone, tone, tone});
            }
        }
        Image image = createImageFrom(bufferedImage);

        int[][] histogram = imagesService.histogram(image);

        for (int i = 0; i < 256; i++) {
            assertEquals(1, histogram[RED][i]);
            assertEquals(1, histogram[GREEN][i]);
            assertEquals(1, histogram[BLUE][i]);
        }
    }
}
