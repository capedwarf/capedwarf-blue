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

package org.jboss.capedwarf.images.transform;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import com.google.appengine.api.images.Transform;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ResizeTransform extends CapedwarfTransform {

    public ResizeTransform(Transform transform) {
        super(transform);
    }

    @Override
    public BufferedImage applyTo(BufferedImage image) {
        double scaleXFactor = (double) getWidth() / image.getWidth();
        double scaleYFactor = (double) getHeight() / image.getHeight();

        if (isCropToFit()) {
            double maxFactor = Math.max(scaleXFactor, scaleYFactor);
            scaleXFactor = maxFactor;
            scaleYFactor = maxFactor;
        } else if (isRetainAspectRatio()) {
            double minFactor = Math.min(scaleXFactor, scaleYFactor);
            scaleXFactor = minFactor;
            scaleYFactor = minFactor;
        }

        AffineTransform tx = AffineTransform.getScaleInstance(scaleXFactor, scaleYFactor);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage transformedImage = op.filter(image, null);

        if (isCropToFit()) {
            transformedImage = transformedImage.getSubimage(
                (int) (getCropOffsetX() * (transformedImage.getWidth() - getWidth())),
                (int) (getCropOffsetY() * (transformedImage.getHeight() - getHeight())),
                getWidth(),
                getHeight());
        }
        return transformedImage;
    }

    private boolean isRetainAspectRatio() {
        return !isAllowStretch();
    }

    private int getWidth() {
        return (Integer) getFieldValue("width");
    }

    private int getHeight() {
        return (Integer) getFieldValue("height");
    }

    private boolean isCropToFit() {
        return (Boolean) getFieldValue("cropToFit");
    }

    private float getCropOffsetX() {
        return (Float) getFieldValue("cropOffsetX");
    }

    private float getCropOffsetY() {
        return (Float) getFieldValue("cropOffsetY");
    }

    private boolean isAllowStretch() {
        return (Boolean) getFieldValue("allowStretch");
    }

}
