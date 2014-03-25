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

package org.jboss.test.capedwarf.images.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.images.support.UrlServingServlet;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.jboss.test.capedwarf.images.support.ImageUtils.assertImagesEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@SuppressWarnings("deprecation")
@RunWith(Arquillian.class)
@Category(All.class)
public class ImageServletTest extends TestBase {

    @Deployment
    public static Archive getDeployment() {
        WebArchive war = getCapedwarfDeployment(TestContext.asDefault().setWebXmlFile("serve_image_web.xml"));
        war.addClass(ImagesServiceTestBase.class);
        war.addClass(IOUtils.class);
        war.addClass(UrlServingServlet.class);
        war.addAsResource("capedwarf.png");
        return war;
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testServletServesCorrectImage(@ArquillianResource URL url) throws Exception {
        BufferedImage servedImage = getServedImage(getImageUrl(url, "basic"));
        BufferedImage storedImage = getCapedwarfPngImage();
        assertImagesEqual(servedImage, storedImage);
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testServletServesResizedImage(@ArquillianResource URL url) throws Exception {
        BufferedImage servedImage = getServedImage(getImageUrl(url, "resized"));
        assertEquals(100, servedImage.getWidth());
        assertEquals(72, servedImage.getHeight());
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void testServletServesCroppedImage(@ArquillianResource URL url) throws Exception {
        BufferedImage servedImage = getServedImage(getImageUrl(url, "cropped"));
        assertEquals(100, servedImage.getWidth());
        assertEquals(100, servedImage.getHeight());
    }


    @Test
    @RunAsClient
    @InSequence(20)
    public void testDeleteServingUrl(@ArquillianResource URL url) throws Exception {
        URL imageUrl = getImageUrl(url, "basic");
        BufferedImage servedImage = getServedImage(imageUrl);
        assertNotNull(servedImage);
        deleteServingUrl(url);
        try {
            getServedImage(imageUrl);
            fail("Expected servlet to return 404, but it did not");
        } catch (FileNotFoundException e) { // thrown when servlet returns 404
            // pass
        }
    }

    private BufferedImage getServedImage(URL imageUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
        try {
            try (InputStream stream = connection.getInputStream()) {
                return ImageIO.read(stream);
            }
        } finally {
            connection.disconnect();
        }
    }

    private BufferedImage getCapedwarfPngImage() throws IOException {
        byte[] storedBytes = IOUtils.toBytes(ImageServletTest.class.getResourceAsStream("/capedwarf.png"), true);
        return ImageIO.read(new ByteArrayInputStream(storedBytes));
    }

    private URL getImageUrl(URL contextUrl, String image) throws IOException {
        return new URL(getResponse(contextUrl, "getImageUrl?image=" + image));
    }

    private void deleteServingUrl(URL contextUrl) throws IOException {
        getResponse(contextUrl, "deleteServingUrl");
    }

    private String getResponse(URL contextUrl, String uri) throws IOException {
        URL url = new URL(contextUrl, uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            byte[] bytes = IOUtils.toBytes(connection.getInputStream(), true);
            return new String(bytes, "UTF-8");
        } finally {
            connection.disconnect();
        }
    }
}
