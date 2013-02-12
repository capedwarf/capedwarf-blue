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

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class BlobstoreUploadTestCase extends BaseTest {

    public static final String UPLOADED_CONTENT = "uploaded content";

    @Deployment
    public static Archive getDeployment() {
        TestContext testContext = new TestContext().setWebXmlFile("upload_blob_web.xml");
        return getCapedwarfDeployment(testContext)
            .addClass(FileUploader.class)
            .addClass(UploadUrlServerServlet.class)
            .addClass(UploadHandlerServlet.class);
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void uploadFile(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"));
        fileUploader.uploadFile(uploadUrl, "file", "uploadedFile.txt", UPLOADED_CONTENT);
    }

    @Test
    @InSequence(20)
    public void testUploadedFileHasCorrectContent() throws Exception {
        BlobKey blobKey = UploadHandlerServlet.getLastUploadedBlobKey();
        assertNotNull("blobKey should not be null", blobKey);

        String contents = getFileContents(blobKey);
        assertEquals(UPLOADED_CONTENT, contents);
    }

    private String getFileContents(BlobKey blobKey) throws IOException {
        AppEngineFile file = getAppEngineFile(blobKey);
        return getFileContents(file);
    }

    private AppEngineFile getAppEngineFile(BlobKey blobKey) {
        FileService fileService = FileServiceFactory.getFileService();
        return fileService.getBlobFile(blobKey);
    }


    private String getFileContents(AppEngineFile file) throws IOException {
        FileReadChannel channel = FileServiceFactory.getFileService().openReadChannel(file, true);
        try {
            return getStringFromChannel(channel, 1000);
        } finally {
            channel.close();
        }
    }

    private String getStringFromChannel(FileReadChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int bytesRead = channel.read(buffer);

        byte[] bytes = new byte[bytesRead == -1 ? 0 : bytesRead];
        buffer.flip();
        buffer.get(bytes);

        return new String(bytes);
    }


}
