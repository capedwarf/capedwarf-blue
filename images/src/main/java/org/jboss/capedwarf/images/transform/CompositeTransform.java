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
import org.jboss.capedwarf.images.JBossImagesService;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A composite transform of multiple transforms.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CompositeTransform extends JBossTransform {

    public CompositeTransform(Transform transform) {
        super(transform);
    }

    @Override
    public BufferedImage applyTo(BufferedImage image) {
        for (Transform transform : getTransforms()) {
            image = JBossImagesService.applyTransform(transform, image);
        }
        return image;
    }

    private List<Transform> getTransforms() {
        return getFieldValue("transforms");
    }

}
