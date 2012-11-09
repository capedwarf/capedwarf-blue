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

import java.awt.image.BufferedImage;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import org.jboss.capedwarf.images.JBossImagesService;
import org.jboss.capedwarf.images.util.ColorUtils;
import org.junit.Before;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossImagesServiceTest extends AbstractImagesServiceTest {

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
}
