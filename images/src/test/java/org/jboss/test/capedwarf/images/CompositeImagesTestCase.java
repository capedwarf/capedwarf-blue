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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import org.jboss.capedwarf.images.util.ColorUtils;
import org.jboss.capedwarf.images.util.ImageUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.google.appengine.api.images.Composite.Anchor.BOTTOM_CENTER;
import static com.google.appengine.api.images.Composite.Anchor.BOTTOM_LEFT;
import static com.google.appengine.api.images.Composite.Anchor.BOTTOM_RIGHT;
import static com.google.appengine.api.images.Composite.Anchor.CENTER_CENTER;
import static com.google.appengine.api.images.Composite.Anchor.CENTER_LEFT;
import static com.google.appengine.api.images.Composite.Anchor.CENTER_RIGHT;
import static com.google.appengine.api.images.Composite.Anchor.TOP_CENTER;
import static com.google.appengine.api.images.Composite.Anchor.TOP_LEFT;
import static com.google.appengine.api.images.Composite.Anchor.TOP_RIGHT;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CompositeImagesTestCase extends CapedwarfImagesServiceTest {

    private static final long BLACK_ARGB = 0xFF000000L;
    private static final long BLUE_ARGB = 0xFF0000FFL;
    private static final float FULL_OPACITY = 1f;

    @Test
    public void compositeCreatesImageOfGivenSize() {
        Image image = imagesService.composite(Collections.<Composite>emptyList(), 123, 456, BLUE_ARGB);
        Assert.assertEquals(123, image.getWidth());
        Assert.assertEquals(456, image.getHeight());
    }

    @Test
    public void compositeWithNoImagesDisplaysBackgroundColorOnly() {
        Image image = imagesService.composite(Collections.<Composite>emptyList(), 1, 1, BLUE_ARGB);
        assertPixelEqual(ColorUtils.toIntArray(BLUE_ARGB), getARGBPixel(image, 0, 0));
    }

    @Test
    public void compositeWithSingleImageDisplaysGivenImage() {
        Image image = createTestImage();

        Composite composite = ImagesServiceFactory.makeComposite(image, 0, 0, FULL_OPACITY, TOP_LEFT);
        Image compositeImage = imagesService.composite(Collections.singleton(composite), image.getWidth(), image.getHeight(), BLACK_ARGB);

        assertImagesEqual(image, compositeImage);
    }

    @Test
    public void compositeWithTwoImagesPaintsSecondImageOnTopOfFirst() {
        Image bottomImage = createSolidColorImage(2, 1, Color.RED);
        Image topImage = createSolidColorImage(1, 1, Color.BLUE);

        Composite bottomComposite = ImagesServiceFactory.makeComposite(bottomImage, 0, 0, FULL_OPACITY, TOP_LEFT);
        Composite topComposite = ImagesServiceFactory.makeComposite(topImage, 0, 0, FULL_OPACITY, TOP_LEFT);
        List<Composite> composites = Arrays.asList(bottomComposite, topComposite);

        int compositeWidth = bottomImage.getWidth();
        int compositeHeight = bottomImage.getHeight();
        Image compositeImage = imagesService.composite(composites, compositeWidth, compositeHeight, BLACK_ARGB);

        Raster topRaster = getRaster(topImage);
        Raster bottomRaster = getRaster(bottomImage);
        Raster compositeRaster = getRaster(compositeImage);

        assertPixelEqual(getPixel(topRaster, 0, 0), getPixel(compositeRaster, 0, 0));
        assertPixelEqual(getPixel(bottomRaster, 1, 0), getPixel(compositeRaster, 1, 0));
    }

    @Test
    public void compositeCorrectlyHandlesAnchors() {
        assertWhitePixelIsAt(0, 0, onCanvasSized(3, 3, whenAnchorIsAt(TOP_LEFT)));
        assertWhitePixelIsAt(1, 0, onCanvasSized(3, 3, whenAnchorIsAt(TOP_CENTER)));
        assertWhitePixelIsAt(2, 0, onCanvasSized(3, 3, whenAnchorIsAt(TOP_RIGHT)));
        assertWhitePixelIsAt(0, 1, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_LEFT)));
        assertWhitePixelIsAt(1, 1, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_CENTER)));
        assertWhitePixelIsAt(2, 1, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_RIGHT)));
        assertWhitePixelIsAt(0, 2, onCanvasSized(3, 3, whenAnchorIsAt(BOTTOM_LEFT)));
        assertWhitePixelIsAt(1, 2, onCanvasSized(3, 3, whenAnchorIsAt(BOTTOM_CENTER)));
        assertWhitePixelIsAt(2, 2, onCanvasSized(3, 3, whenAnchorIsAt(BOTTOM_RIGHT)));
    }

    @Test
    public void compositeCorrectlyHandlesXOffset() {
        assertWhitePixelIsAt(0, 1, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_CENTER), andXOffsetIs(-1)));
        assertWhitePixelIsAt(2, 1, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_CENTER), andXOffsetIs(+1)));
    }

    @Test
    public void compositeCorrectlyHandlesYOffset() {
        assertWhitePixelIsAt(1, 0, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_CENTER), andYOffsetIs(-1)));
        assertWhitePixelIsAt(1, 2, onCanvasSized(3, 3, whenAnchorIsAt(CENTER_CENTER), andYOffsetIs(+1)));
    }

    private Image onCanvasSized(int width, int height, Composite.Anchor anchor) {
        return onCanvasSized(width, height, anchor, new Point(0, 0));
    }

    private Image onCanvasSized(int width, int height, Composite.Anchor anchor, Point offset) {
        return createWhitePixelOnBlackCanvas(anchor, width, height, offset.x, offset.y);
    }

    private Point andXOffsetIs(int xOffset) {
        return new Point(xOffset, 0);
    }

    private Point andYOffsetIs(int yOffset) {
        return new Point(0, yOffset);
    }

    private Composite.Anchor whenAnchorIsAt(Composite.Anchor anchor) {
        return anchor;
    }

    private void assertWhitePixelIsAt(int x, int y, Image compositeImage) {
        Raster compositeRaster = getRaster(compositeImage);
        int[] WHITE = {255, 255, 255, 255};
        int[] pixel = getPixel(compositeRaster, x, y);
        assertPixelEqual(WHITE, pixel);
    }

    private Image createWhitePixelOnBlackCanvas(Composite.Anchor anchor, int width, int height, int xOffset, int yOffset) {
        Image pixelImage = createSolidColorImage(1, 1, Color.WHITE);
        Composite composite = ImagesServiceFactory.makeComposite(pixelImage, xOffset, yOffset, FULL_OPACITY, anchor);
        return imagesService.composite(Collections.singleton(composite), width, height, BLACK_ARGB);
    }

    private Image createSolidColorImage(int width, int height, Color color) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = bufferedImage.getGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, width, height);
        g2.dispose();

        return createImageFrom(bufferedImage);
    }

    private void dumpImage(Image image, String filename) {
        try {
            BufferedImage bufferedImage = ImageUtils.convertToBufferedImage(image.getImageData());
            if (!filename.endsWith(".png")) {
                filename += ".png";
            }
            ImageIO.write(bufferedImage, "PNG", new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
