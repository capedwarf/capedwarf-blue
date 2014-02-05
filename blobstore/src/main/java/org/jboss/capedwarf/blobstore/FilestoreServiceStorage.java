/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.blobstore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.files.ExposedFileService;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("deprecation")
class FilestoreServiceStorage implements Storage {
    private ExposedFileService fileService;

    protected synchronized ExposedFileService getFileService() {
        if (fileService == null) {
            fileService = (ExposedFileService) FileServiceFactory.getFileService();
        }
        return fileService;
    }

    public BlobKey store(StreamProvider provider, String filename, String contentType, String bucketName) throws IOException {
        ExposedFileService fileService = getFileService();

        AppEngineFile file;
        if (bucketName == null) {
            file = fileService.createNewBlobFile(contentType, filename);
        } else {
            file = fileService.createNewBlobFile(contentType, bucketName, filename, AppEngineFile.FileSystem.GS);
        }

        try (ReadableByteChannel in = Channels.newChannel(provider.get())) {
            FileWriteChannel out = fileService.openWriteChannel(file, true);
            try {
                IOUtils.copy(in, out);
            } finally {
                out.closeFinally();
            }
        }

        return fileService.getBlobKey(file);
    }

    public void delete(BlobKey... keys) {
        getFileService().delete(keys);
    }

    public BlobKey getBlobKey(AppEngineFile file) {
        return getFileService().getBlobKey(file);
    }

    public InputStream getStream(BlobKey key) throws IOException {
        return getFileService().getStream(key);
    }

    public BlobInfo getBlobInfo(BlobKey key) {
        return getFileService().getBlobInfo(key);
    }

    public FileInfo getFileInfo(BlobKey key) {
        return getFileService().getFileInfo(key);
    }
}
