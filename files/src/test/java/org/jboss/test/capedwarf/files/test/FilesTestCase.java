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

package org.jboss.test.capedwarf.files.test;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.RecordWriteChannel;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class FilesTestCase {

    private FileService service;

    @Before
    public void setUp() throws Exception {
        service = FileServiceFactory.getFileService();
    }

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine/appengine-web.xml", "appengine-web.xml");
    }

    @Test
    public void testCreateNewBlobFile() throws Exception {
        AppEngineFile file = service.createNewBlobFile("image/jpeg", "created.jpg");
        assertEquals(AppEngineFile.FileSystem.BLOBSTORE, file.getFileSystem());
    }

    @Test
    public void testCloseWithoutWritingAnything() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "empty.txt");
        FileWriteChannel channel = service.openWriteChannel(file, true);
        channel.closeFinally();

        assertTrue(getFileContents(file).isEmpty());
    }

    @Test
    public void testSingleWrite() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "single.txt");
        writeToFileAndFinalize(file, "some-bytes");
        assertEquals("some-bytes", getFileContents(file));
    }

    @Test
    public void testAppend() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "append.txt");
        writeToFile(file, "some-bytes");
        writeToFileAndFinalize(file, " appended-bytes");
        assertEquals("some-bytes appended-bytes", getFileContents(file));
    }

    @Test
    public void testPosition() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "position.txt");
        writeToFileAndFinalize(file, "0123456789");

        FileReadChannel channel = service.openReadChannel(file, false);

        channel.position(5);
        assertEquals("567", getStringFromChannel(channel, 3));

        channel.position(2);
        assertEquals("234", getStringFromChannel(channel, 3));
    }

    @Test(expected = FileNotFoundException.class)
    public void testFileNotFound() throws Exception {
        AppEngineFile nonExistentFile = new AppEngineFile(AppEngineFile.FileSystem.BLOBSTORE, "nonExistentFile.txt");
        service.openReadChannel(nonExistentFile, false);
    }

    @Test(expected = FinalizationException.class)
    public void testFileNotReadableUntilFinalized() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "notFinalized.txt");
        writeToFile(file, "some-bytes");    // NOTE: file is not finalized

        service.openReadChannel(file, false);
    }

    @Test
    public void testBlobKey() throws Exception {
        AppEngineFile file = service.createNewBlobFile("image/jpeg");
        writeToFileAndFinalize(file, "some-bytes");
        BlobKey blobKey = service.getBlobKey(file);

        AppEngineFile file2 = service.getBlobFile(blobKey);
        String contents = getFileContents(file2);
        assertEquals("some-bytes", contents);
    }

    @Test
    public void testBlobInfo() throws Exception {
        BlobInfoFactory blobInfoFactory = new BlobInfoFactory();

        AppEngineFile file = service.createNewBlobFile("text/plain", "blobInfo.txt");
        writeToFileAndFinalize(file, "some-bytes");

        BlobKey blobKey = service.getBlobKey(file);
        BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);

        assertEquals(blobKey, blobInfo.getBlobKey());
        assertEquals("text/plain", blobInfo.getContentType());
        assertEquals("blobInfo.txt", blobInfo.getFilename());
        assertEquals("some-bytes".length(), blobInfo.getSize());
        assertNotNull(blobInfo.getCreation());

        // TODO: test MD5 hash
    }


    @Test
    public void testRecordChannel() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "records.txt");
        RecordWriteChannel channel = service.openRecordWriteChannel(file, true);
        channel.closeFinally();

        assertTrue(getFileContents(file).isEmpty());
    }


    private void writeToFile(AppEngineFile file, String content) throws IOException {
        writeToFile(file, content, false);
    }

    private void writeToFileAndFinalize(AppEngineFile file, String content) throws IOException {
        writeToFile(file, content, true);
    }

    private void writeToFile(AppEngineFile file, String content, boolean finalize) throws IOException {
        FileWriteChannel channel = service.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(content.getBytes()));
        } finally {
            if (finalize) {
                channel.closeFinally();
            } else {
                channel.close();
            }
        }
    }

    private String getFileContents(AppEngineFile file) throws IOException {
        FileReadChannel channel = service.openReadChannel(file, true);
        try {
            return getStringFromChannel(channel, 1000);
        } finally {
            channel.close();
        }
    }

    private String getStringFromChannel(FileReadChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int bytesRead = channel.read(buffer);

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);

        return new String(bytes);
    }
}
