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

import com.google.appengine.api.images.Composite;
import org.jboss.capedwarf.images.util.ColorUtils;
import org.jboss.capedwarf.images.util.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * Build a composite BufferedImage from collection of GAE Composites.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CompositeImageBuilder {

    private Collection<Composite> composites;
    private int width;
    private int height;
    private long backgroundColor;

    public CompositeImageBuilder(Collection<Composite> composites, int width, int height, long backgroundColor) {
        this.composites = composites;
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
    }

    public BufferedImage createBufferedImage() {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        drawOntoCanvas(canvas);
        return canvas;
    }

    private void drawOntoCanvas(BufferedImage canvas) {
        Graphics2D g2 = canvas.createGraphics();
        drawToGraphics(g2, canvas);
        g2.dispose();
    }

    private void drawToGraphics(Graphics2D g2, BufferedImage canvas) {
        paintBackground(g2);
        paintComposites(g2, canvas);
    }

    private void paintComposites(Graphics2D g2, BufferedImage canvas) {
        for (Composite composite : composites) {
            CompositeWrapper compositeWrapper = new CompositeWrapper(composite);
            paintComposite(g2, canvas, compositeWrapper);
        }
    }

    private void paintComposite(Graphics2D g2, BufferedImage canvas, CompositeWrapper composite) {
        BufferedImage image = getBufferedImage(composite);

        int x = getAnchorX(composite, canvas, image) + composite.getXOffset();
        int y = getAnchorY(composite, canvas, image) + composite.getYOffset();
        g2.drawImage(image, x, y, null);
    }


    private int getAnchorX(CompositeWrapper composite, BufferedImage canvas, BufferedImage image) {
        switch (composite.getAnchor()) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                return 0;
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                return canvas.getWidth() - image.getWidth();
            case TOP_CENTER:
            case CENTER_CENTER:
            case BOTTOM_CENTER:
                return (canvas.getWidth() - image.getWidth()) / 2;
            default:
                throw new IllegalArgumentException("Unsupported anchor " + composite.getAnchor());
        }
    }

    private int getAnchorY(CompositeWrapper composite, BufferedImage canvas, BufferedImage image) {
        switch (composite.getAnchor()) {
            case TOP_LEFT:
            case TOP_RIGHT:
            case TOP_CENTER:
                return 0;
            case CENTER_LEFT:
            case CENTER_RIGHT:
            case CENTER_CENTER:
                return (canvas.getHeight() - image.getHeight()) / 2;
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
            case BOTTOM_CENTER:
                return canvas.getHeight() - image.getHeight();
            default:
                throw new IllegalArgumentException("Unsupported anchor " + composite.getAnchor());
        }
    }

    private BufferedImage getBufferedImage(CompositeWrapper composite) {
        return ImageUtils.convertToBufferedImage(composite.getImage().getImageData());
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(ColorUtils.fromLongARGB(backgroundColor));
        g2.fillRect(0, 0, width, height);
    }
}
