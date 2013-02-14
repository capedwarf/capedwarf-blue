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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class MakeImageTest extends ImagesServiceTestBase {

    @Test
    public void makeImageCanReadJPG() throws IOException {
        byte[] imageData = readImageResource(CAPEDWARF_JPG);
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadPNG() throws IOException {
        byte[] imageData = readImageResource(CAPEDWARF_PNG);
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadGIF() throws IOException {
        byte[] imageData = readImageResource(CAPEDWARF_GIF);
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadBMP() throws IOException {
        byte[] imageData = readImageResource(CAPEDWARF_BMP);
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadTIF() throws IOException {
        byte[] imageData = readImageResource(CAPEDWARF_TIF);
        assertMakeImageCanReadImage(imageData);
    }

    private void assertMakeImageCanReadImage(byte[] imageData) {
        Image image = ImagesServiceFactory.makeImage(imageData);
        assertNotNull(image);
    }

    private byte[] readImageResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        return IOUtils.toBytes(inputStream, true);
    }

}
