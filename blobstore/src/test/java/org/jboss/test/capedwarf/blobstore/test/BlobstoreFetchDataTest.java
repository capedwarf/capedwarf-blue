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

package org.jboss.test.capedwarf.blobstore.test;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class BlobstoreFetchDataTest extends BlobstoreTestBase {

    private BlobstoreService blobstore;
    private BlobKey blobKey;

    @Deployment
    public static Archive getDeployment() {
        return getCapedwarfDeployment()
            .addClass(BlobstoreTestBase.class);
    }

    @Before
    public void setUp() throws Exception {
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        blobKey = writeNewBlobFile("Uploaded text");
    }

    @After
    public void tearDown() {
        blobstore.delete(blobKey);
    }

    @Test
    public void testFetchDataHandlesIndexesCorrectly() {
        assertEquals("Uploaded text", new String(blobstore.fetchData(blobKey, 0, 100)));
        assertEquals("Upload", new String(blobstore.fetchData(blobKey, 0, 5)));
        assertEquals("loaded", new String(blobstore.fetchData(blobKey, 2, 7)));
    }

    @Test
    public void testFetchDataThrowsIAEWhenBlobKeyDoesNotExist() {
        assertFetchDataThrowsIAE(new BlobKey("nonexistent"), 0, 10);
    }

    @Test
    public void testFetchDataThrowsIAEWhenIndexNegative() {
        assertFetchDataThrowsIAE(blobKey, -1, 10);
        assertFetchDataThrowsIAE(blobKey, 0, -10);
    }

    @Test
    public void testFetchDataThrowsIAEWhenEndIndexLessThanStartIndex() {
        assertFetchDataThrowsIAE(blobKey, 10, 5);
    }

    @Test
    public void testFetchDataHonorsMaxBlobFetchSize() throws Exception {
        // NOTE: endIndex is inclusive, so we're actually fetching (endIndex - startIndex + 1) bytes
        assertEquals("Uploaded text", new String(blobstore.fetchData(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE - 1)));
        assertFetchDataThrowsIAE(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE);
    }

    private void assertFetchDataThrowsIAE(BlobKey blobKey, int startIndex, int endIndex) {
        try {
            blobstore.fetchData(blobKey, startIndex, endIndex);
            fail("Expected IllegalArgumentException when invoking fetchData(blobKey, " + startIndex + ", " + endIndex + ")");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

}
