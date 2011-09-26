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

package org.jboss.capedwarf.blobstore;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.blobstore.UnsupportedRangeFormatException;
import com.google.appengine.api.blobstore.UploadOptions;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossBlobstoreService implements BlobstoreService {

    static final String SERVE_HEADER = "X-AppEngine-BlobKey";
    static final String UPLOADED_BLOBKEY_ATTR = "com.google.appengine.api.blobstore.upload.blobkeys";
    static final String BLOB_RANGE_HEADER = "X-AppEngine-BlobRange";

    public String createUploadUrl(String successPath, UploadOptions uploadOptions) {
        return null;  // TODO
    }

    public void delete(BlobKey... blobKeys) {
        GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
        for (BlobKey key : blobKeys)
            gfs.remove(key.getKeyString(), true);
    }

    public void serve(BlobKey blobKey, ByteRange byteRange, HttpServletResponse response) {
        if (response.isCommitted()) {
            throw new IllegalStateException("Response was already committed.");
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(SERVE_HEADER, blobKey.getKeyString());
        if (byteRange != null) {
            response.setHeader(BLOB_RANGE_HEADER, byteRange.toString());
        }

        // TODO
    }

    public byte[] fetchData(BlobKey blobKey, long start, long end) {
        try {
            GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
            return IOUtils.toBytes(gfs.getInput(blobKey.getKeyString()), start, end, true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // ------

    public String createUploadUrl(String successPath) {
        return createUploadUrl(successPath, UploadOptions.Builder.withDefaults());
    }

    public void serve(BlobKey blobKey, HttpServletResponse response) {
        serve(blobKey, (ByteRange) null, response);
    }

    public void serve(BlobKey blobKey, String rangeHeader, HttpServletResponse response) {
        serve(blobKey, ByteRange.parse(rangeHeader), response);
    }

    @SuppressWarnings("unchecked")
    public ByteRange getByteRange(HttpServletRequest request) {
        Enumeration<String> rangeHeaders = request.getHeaders("range");
        if (rangeHeaders.hasMoreElements() == false) {
            return null;
        }

        String rangeHeader = rangeHeaders.nextElement();
        if (rangeHeaders.hasMoreElements()) {
            throw new UnsupportedRangeFormatException("Cannot accept multiple range headers.");
        }

        return ByteRange.parse(rangeHeader);
    }

    @SuppressWarnings("unchecked")
    public Map<String, BlobKey> getUploadedBlobs(HttpServletRequest request) {
        Map<String, String> attributes = (Map<String, String>) request.getAttribute(UPLOADED_BLOBKEY_ATTR);
        if (attributes == null) {
            throw new IllegalStateException("Must be called from a blob upload callback request.");
        }
        Map<String, BlobKey> blobKeys = new HashMap<String, BlobKey>(attributes.size());
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            blobKeys.put(attr.getKey(), new BlobKey(attr.getValue()));
        }
        return blobKeys;
    }
}
