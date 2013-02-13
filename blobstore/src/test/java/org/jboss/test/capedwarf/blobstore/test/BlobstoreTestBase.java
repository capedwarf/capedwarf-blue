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
import java.nio.ByteBuffer;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import org.jboss.test.capedwarf.common.test.TestBase;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BlobstoreTestBase extends TestBase {

    protected BlobKey writeNewBlobFile(String text) throws IOException {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("text/plain", "uploadedText.txt");
        FileWriteChannel channel = fileService.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(text.getBytes()));
        } finally {
            channel.closeFinally();
        }
        return fileService.getBlobKey(file);
    }
}
