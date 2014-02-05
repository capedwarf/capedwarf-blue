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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import org.jboss.capedwarf.common.io.IOUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class GcsServiceStorage extends FilestoreServiceStorage {
    private GcsService gcsService;

    protected synchronized GcsService getGcsService() {
        if (gcsService == null) {
            gcsService = GcsServiceFactory.createGcsService();
        }
        return gcsService;
    }

    protected static GcsFilename toFilename(BlobKey key) {
        String path = key.getKeyString().substring(3); // gs/ == 3
        int p = path.lastIndexOf("/");
        return new GcsFilename(path.substring(0, p), path.substring(p + 1));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlobKey store(StreamProvider provider, String filename, String contentType, String bucketName) throws IOException {
        if (bucketName == null) {
            return super.store(provider, filename, contentType, bucketName);
        } else {
            AppEngineFile file = getFileService().createNewBlobFile(contentType, bucketName, filename, AppEngineFile.FileSystem.GS);
            BlobKey blobKey = getBlobKey(file);
            GcsFileOptions options = new GcsFileOptions.Builder().mimeType(contentType).build();
            try (GcsOutputChannel out = getGcsService().createOrReplace(toFilename(blobKey), options)) {
                try (ReadableByteChannel in = Channels.newChannel(provider.get())) {
                    IOUtils.copy(in, out);
                }
            }
            return blobKey;
        }
    }

    @Override
    public void delete(BlobKey... keys) {
        for (BlobKey key : keys) {
            if (key.getKeyString().startsWith("gs/")) {
                try {
                    getGcsService().delete(toFilename(key));
                } catch (IOException e) {
                    throw new IllegalStateException(String.format("I/O error during delete, key: %s", key), e);
                }
            } else {
                super.delete(key);
            }
        }
    }
}
