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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.blobstore.UnsupportedRangeFormatException;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.jboss.capedwarf.files.JBossFileService;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossBlobstoreService implements BlobstoreService {

    private static final String SERVE_HEADER = "X-AppEngine-BlobKey";
    private static final String UPLOADED_BLOBKEY_ATTR = "com.google.appengine.api.blobstore.upload.blobkeys";
    private static final String BLOB_RANGE_HEADER = "X-AppEngine-BlobRange";

    public String createUploadUrl(String successPath) {
        return createUploadUrl(successPath, UploadOptions.Builder.withDefaults());
    }

    public String createUploadUrl(String successPath, UploadOptions uploadOptions) {
        return UploadServlet.createUploadUrl(successPath, uploadOptions);
    }

    public void delete(BlobKey... blobKeys) {
        getFileService().delete(blobKeys);
    }

    public void serve(BlobKey blobKey, HttpServletResponse response) throws IOException {
        serve(blobKey, (ByteRange) null, response);
    }

    public void serve(BlobKey blobKey, String rangeHeader, HttpServletResponse response) throws IOException {
        serve(blobKey, ByteRange.parse(rangeHeader), response);
    }

    public void serve(BlobKey blobKey, ByteRange byteRange, HttpServletResponse response) throws IOException {
        assertNotCommited(response);
        try {
            InputStream in = getStream(blobKey);
            try {
                response.setStatus(HttpServletResponse.SC_OK);
                setHeaders(response, blobKey, byteRange);
                copyStream(in, response.getOutputStream(), byteRange);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void assertNotCommited(HttpServletResponse response) {
        if (response.isCommitted()) {
            throw new IllegalStateException("Response was already committed.");
        }
    }

    private void setHeaders(HttpServletResponse response, BlobKey blobKey, ByteRange byteRange) {
        response.setHeader(SERVE_HEADER, blobKey.getKeyString());
        if (byteRange != null) {
            response.setHeader(BLOB_RANGE_HEADER, byteRange.toString());
        }
    }

    private void copyStream(InputStream in, OutputStream out, ByteRange range) throws IOException {
        if (range == null) {
            IOUtils.copyStream(in, out);
        } else {
            if (range.hasEnd()) {
                long length = range.getEnd() + 1 - range.getStart();  // end is inclusive, hence +1
                IOUtils.copyStream(in, out, range.getStart(), length);
            } else {
                IOUtils.copyStream(in, out, range.getStart());
            }
        }
    }

    public byte[] fetchData(BlobKey blobKey, long start, long end) {
        try {
            InputStream stream = getStream(blobKey);
            return IOUtils.toBytes(stream, start, end, true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public ByteRange getByteRange(HttpServletRequest request) {
        Enumeration<String> rangeHeaders = request.getHeaders("range");
        if (!rangeHeaders.hasMoreElements()) {
            return null;
        }

        String rangeHeader = rangeHeaders.nextElement();
        if (rangeHeaders.hasMoreElements()) {
            throw new UnsupportedRangeFormatException("Cannot accept multiple range headers.");
        }

        return ByteRange.parse(rangeHeader);
    }

    public void storeUploadedBlobs(HttpServletRequest request) throws IOException, ServletException {
        Map<String, BlobKey> map = new HashMap<String, BlobKey>();
        for (Part part : request.getParts()) {
            if (ServletUtils.isFile(part)) {
                BlobKey blobKey = storeUploadedBlob(part);
                map.put(part.getName(), blobKey);
            }
        }

        request.setAttribute(UPLOADED_BLOBKEY_ATTR, map);
    }

    private BlobKey storeUploadedBlob(Part part) throws IOException {
        JBossFileService fileService = getFileService();
        AppEngineFile file = fileService.createNewBlobFile(part.getContentType(), ServletUtils.getFileName(part));

        ReadableByteChannel in = Channels.newChannel(part.getInputStream());
        try {
            FileWriteChannel out = fileService.openWriteChannel(file, true);
            try {
                IOUtils.copy(in, out);
            } finally {
                out.closeFinally();
            }
        } finally {
            in.close();
        }

        return fileService.getBlobKey(file);
    }

    @SuppressWarnings("unchecked")
    public Map<String, BlobKey> getUploadedBlobs(HttpServletRequest request) {
        Map<String, BlobKey> map = (Map<String, BlobKey>) request.getAttribute(UPLOADED_BLOBKEY_ATTR);
        if (map == null) {
            throw new IllegalStateException("Must be called from a blob upload callback request.");
        }
        return map;
    }

    public Map<String, List<BlobKey>> getUploads(HttpServletRequest httpServletRequest) {
        return new HashMap<String, List<BlobKey>>(); // TODO
    }

    public InputStream getStream(BlobKey blobKey) throws FileNotFoundException {
        return getFileService().getStream(blobKey);
    }

    private JBossFileService getFileService() {
        return (JBossFileService) FileServiceFactory.getFileService();
    }

    public BlobKey createGsBlobKey(String name) {
        return null; // TODO
    }
}
