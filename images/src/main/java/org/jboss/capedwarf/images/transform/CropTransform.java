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

package org.jboss.capedwarf.images.transform;

import com.google.appengine.api.images.Transform;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CropTransform extends JBossTransform {

    public CropTransform(Transform transform) {
        super(transform);
    }

    @Override
    public BufferedImage applyTo(BufferedImage image) {
        int leftXInt = convertX(getLeftX(), image);
        int rightXInt = convertX(getRightX(), image);
        int topYInt = convertY(getTopY(), image);
        int bottomYInt = convertY(getBottomY(), image);

        int croppedWidth = rightXInt - leftXInt;
        int croppedHeight = bottomYInt - topYInt;

        return image.getSubimage(leftXInt, topYInt, croppedWidth, croppedHeight);
    }

    private Float getBottomY() {
        return getFieldValue("bottomY");
    }

    private Float getRightX() {
        return getFieldValue("rightX");
    }

    private Float getTopY() {
        return getFieldValue("topY");
    }

    private Float getLeftX() {
        return getFieldValue("leftX");
    }

    private int convertX(float x, BufferedImage image) {
        return (int) (x * image.getWidth());
    }

    private int convertY(float y, BufferedImage image) {
        return (int) (y * image.getHeight());
    }
}
