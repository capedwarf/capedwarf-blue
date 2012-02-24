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

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.images.JBossImagesService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class IntegrationTestCase {

    private static final String TEST_IMAGE_RESOURCE = "capedwarf.png";

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml")
                .addAsResource(TEST_IMAGE_RESOURCE);
    }

    @Test
    public void factoryReturnsJBossImplementation() {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        assertEquals(JBossImagesService.class, imagesService.getClass());
    }

    @Test
    public void testMakeImage() {
        Image image = loadTestImage();
        assertNotNull(image);
    }

    private Image loadTestImage() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEST_IMAGE_RESOURCE);
            byte[] byteArray = IOUtils.toBytes(inputStream, true);
            return ImagesServiceFactory.makeImage(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHorizontalFlip() {
        Image image = loadTestImage();
        Transform horizontalFlip = ImagesServiceFactory.makeHorizontalFlip();

        Image flippedImage = ImagesServiceFactory.getImagesService().applyTransform(horizontalFlip, image);

        assertNotNull(flippedImage);
    }


}
