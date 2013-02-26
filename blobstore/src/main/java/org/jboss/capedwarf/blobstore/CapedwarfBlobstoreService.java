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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.ByteRange;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.blobstore.RangeFormatException;
import com.google.appengine.api.blobstore.UnsupportedRangeFormatException;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.common.servlet.ServletUtils;
import org.jboss.capedwarf.files.ExposedFileService;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class CapedwarfBlobstoreService implements ExposedBlobstoreService {
    private static final String UPLOADED_BLOBKEY_ATTR = "com.google.appengine.api.blobstore.upload.blobkeys";
    private static final String UPLOADED_BLOBKEY_LIST_ATTR = "com.google.appengine.api.blobstore.upload.blobkeylists";

    private Function<List<BlobKey>, List<BlobInfo>> BLOB_LIST_KEY_TO_INFO_FN = new Function<List<BlobKey>, List<BlobInfo>>() {
        public List<BlobInfo> apply(List<BlobKey> input) {
            return Lists.transform(input, BLOB_KEY_TO_INFO_FN);
        }
    };

    private Function<BlobKey, BlobInfo> BLOB_KEY_TO_INFO_FN = new Function<BlobKey, BlobInfo>() {
        public BlobInfo apply(BlobKey input) {
            return getBlobInfo(input);
        }
    };

    private Function<List<BlobKey>, List<FileInfo>> FILE_LIST_KEY_TO_INFO_FN = new Function<List<BlobKey>, List<FileInfo>>() {
        public List<FileInfo> apply(List<BlobKey> input) {
            return Lists.transform(input, FILE_KEY_TO_INFO_FN);
        }
    };

    private Function<BlobKey, FileInfo> FILE_KEY_TO_INFO_FN = new Function<BlobKey, FileInfo>() {
        public FileInfo apply(BlobKey input) {
            return getFileService().getFileInfo(input);
        }
    };

    private ExposedFileService fileService;

    private synchronized ExposedFileService getFileService() {
        if (fileService == null) {
            fileService = (ExposedFileService) FileServiceFactory.getFileService();
        }
        return fileService;
    }

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

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(BLOB_KEY_HEADER, blobKey.getKeyString());
        if (byteRange != null) {
            response.setHeader(BLOB_RANGE_HEADER, byteRange.toString());
        }
    }

    public void serveBlob(BlobKey blobKey, String byteRangeStr, HttpServletResponse response) throws IOException {
        assertNotCommited(response);

        BlobInfo blobInfo = getBlobInfo(blobKey);
        response.setContentType(blobInfo.getContentType());

        ByteRange byteRange = null;

        if (byteRangeStr == null) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            try {
                byteRange = ByteRange.parse(byteRangeStr);
            } catch (RangeFormatException e) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            if (byteRange.getStart() >= blobInfo.getSize()) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            if (byteRange.hasEnd() && byteRange.getEnd() >= blobInfo.getSize()) {
                byteRange = new ByteRange(byteRange.getStart(), blobInfo.getSize()-1);
            }

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + byteRange.getStart() + "-" + byteRange.getEnd() + "/" + blobInfo.getSize());
        }

        try {
            InputStream in = getStream(blobKey);
            try {
                copyStream(in, response.getOutputStream(), byteRange);
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private BlobInfo getBlobInfo(BlobKey blobKey) {
        return getFileService().getBlobInfo(blobKey);
    }

    private void assertNotCommited(HttpServletResponse response) {
        if (response.isCommitted()) {
            throw new IllegalStateException("Response was already committed.");
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

    public byte[] fetchData(BlobKey blobKey, long startIndex, long endIndex) {
        if (startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be >= 0");
        }

        if (endIndex < startIndex) {
            throw new IllegalArgumentException("endIndex must be >= startIndex");
        }

        long fetchSize = endIndex - startIndex + 1;
        if (fetchSize > MAX_BLOB_FETCH_SIZE) {
            throw new IllegalArgumentException("Blob fetch size " + fetchSize + " is larger than MAX_BLOB_FETCH_SIZE (" + MAX_BLOB_FETCH_SIZE + ")");
        }

        try {
            InputStream stream = getStream(blobKey);
            return IOUtils.toBytes(stream, startIndex, endIndex, true);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Blob does not exist");
        } catch (IOException e) {
            throw new BlobstoreFailureException("An unexpected error occured", e);
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
        Map<String, List<BlobKey>> map2 = new HashMap<String, List<BlobKey>>();
        for (Part part : request.getParts()) {
            if (ServletUtils.isFile(part)) {
                BlobKey blobKey = storeUploadedBlob(part);
                String name = part.getName();
                map.put(name, blobKey);
                List<BlobKey> list = map2.get(name);
                if (list == null) {
                    list = new LinkedList<BlobKey>();
                    map2.put(name, list);
                }
                list.add(blobKey);
            }
        }

        request.setAttribute(UPLOADED_BLOBKEY_ATTR, map);
        request.setAttribute(UPLOADED_BLOBKEY_LIST_ATTR, map2);
    }

    private BlobKey storeUploadedBlob(Part part) throws IOException {
        ExposedFileService fileService = getFileService();
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

    @SuppressWarnings("unchecked")
    public Map<String, List<BlobKey>> getUploads(HttpServletRequest request) {
        Map<String, List<BlobKey>> map = (Map<String, List<BlobKey>>) request.getAttribute(UPLOADED_BLOBKEY_LIST_ATTR);
        if (map == null) {
            throw new IllegalStateException("Must be called from a blob upload callback request.");
        }
        return map;
    }

    public InputStream getStream(BlobKey blobKey) throws FileNotFoundException {
        return getFileService().getStream(blobKey);
    }

    public BlobKey createGsBlobKey(String name) {
        return null; // TODO
    }

    public Map<String, List<BlobInfo>> getBlobInfos(HttpServletRequest httpServletRequest) {
        return Maps.transformValues(getUploads(httpServletRequest), BLOB_LIST_KEY_TO_INFO_FN);
    }

    public Map<String, List<FileInfo>> getFileInfos(HttpServletRequest httpServletRequest) {
        return Maps.transformValues(getUploads(httpServletRequest), FILE_LIST_KEY_TO_INFO_FN);
    }
}
