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

package org.jboss.test.capedwarf.images.test;

import java.io.IOException;
import java.io.InputStream;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class MakeImageTestCase {

    @Test
    public void makeImageCanReadJPG() throws IOException {
        byte[] imageData = readImageResource("capedwarf.jpg");
        Image image = ImagesServiceFactory.makeImage(imageData);
        Assert.assertNotNull(image);
    }

    @Test
    public void makeImageCanReadPNG() throws IOException {
        byte[] imageData = readImageResource("capedwarf.png");
        Image image = ImagesServiceFactory.makeImage(imageData);
        Assert.assertNotNull(image);
    }

    @Test
    public void makeImageCanReadGIF() throws IOException {
        byte[] imageData = readImageResource("capedwarf.gif");
        Image image = ImagesServiceFactory.makeImage(imageData);
        Assert.assertNotNull(image);
    }

    @Test
    public void makeImageCanReadBMP() throws IOException {
        byte[] imageData = readImageResource("capedwarf.bmp");
        Image image = ImagesServiceFactory.makeImage(imageData);
        Assert.assertNotNull(image);
    }

    @Test
    public void makeImageCanReadTIF() throws IOException {
        byte[] imageData = readImageResource("capedwarf.tif");
        Image image = ImagesServiceFactory.makeImage(imageData);
        Assert.assertNotNull(image);
    }

    private byte[] readImageResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        return IOUtils.toBytes(inputStream, true);
    }

}
