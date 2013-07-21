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

package org.jboss.test.capedwarf.blobstore.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.blobstore.support.ServeBlobServlet;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class BlobstoreTest extends BlobstoreTestBase {
    @Deployment
    public static Archive getDeployment() {
        TestContext testContext = TestContext.asDefault().setWebXmlFile("serve_blob_web.xml");
        return getCapedwarfDeployment(testContext)
            .addClass(BlobstoreTestBase.class)
            .addClass(IOUtils.class)
            .addClass(ServeBlobServlet.class);
    }

    @Test
    @RunAsClient
    public void testBlobServedWhenResponseContainsBlobKeyHeader(@ArquillianResource URL url) throws Exception {
        String MIME_TYPE = "foo/bar";
        String CONTENTS = "foobar";
        URL pageUrl = new URL(url, "serveblob?name=testblob.txt&mimeType=" + MIME_TYPE + "&contents=" + CONTENTS);

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            assertTrue(connection.getContentType().startsWith(MIME_TYPE));
            assertEquals(CONTENTS, response);
            assertNull("header should have been removed from response", connection.getHeaderField("X-AppEngine-BlobKey"));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testOnlyPartOfBlobServedWhenResponseContainsBlobRangeHeader(@ArquillianResource URL url) throws Exception {
        String CONTENTS = "abcdefghijklmnopqrstuvwxyz";
        URL pageUrl = new URL(url, "serveblob?name=testrange.txt&mimeType=text/plain&contents=" + CONTENTS + "&blobRange=bytes=2-5");

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            int PARTIAL_CONTENT = 206;
            assertEquals(PARTIAL_CONTENT, connection.getResponseCode());
            assertEquals("bytes 2-5/26", connection.getHeaderField("Content-Range"));
            assertEquals(CONTENTS.substring(2, 5 + 1), response);
            assertNull("header should have been removed from response", connection.getHeaderField("X-AppEngine-BlobRange"));

        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testBlobRangeEndGreaterThanContentSize(@ArquillianResource URL url) throws Exception {
        String CONTENTS = "abcdefghijklmnopqrstuvwxyz";
        URL pageUrl = new URL(url, "serveblob?name=testrangeend.txt&mimeType=text/plain&contents=" + CONTENTS + "&blobRange=bytes=2-1000");

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            int PARTIAL_CONTENT = 206;
            assertEquals(PARTIAL_CONTENT, connection.getResponseCode());
            assertEquals("bytes 2-25/26", connection.getHeaderField("Content-Range"));
            assertEquals(CONTENTS.substring(2), response);
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testRequestedRangeNotSatisfiableWhenBlobRangeHeaderIsInvalid(@ArquillianResource URL url) throws Exception {
        int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "invalidBlobRange"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes="));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=1000-0"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=-1-5"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=1000-2000"));
    }

    private void assertServletReturnsResponseCode(int responseCode, URL pageUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            assertEquals(responseCode, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    private URL urlWithBlobRange(URL url, String blobRange) throws MalformedURLException {
        String contents = "whatever";
        return new URL(url, "serveblob?name=testinvalidrange.txt&mimeType=text/plain&contents=" + contents + "&blobRange=" + blobRange);
    }

    private String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString();
        } finally {
            in.close();
        }
    }

}
