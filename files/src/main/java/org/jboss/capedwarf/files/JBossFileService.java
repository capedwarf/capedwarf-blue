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

package org.jboss.capedwarf.files;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServicePb;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.appengine.api.files.RecordReadChannel;
import com.google.appengine.api.files.RecordWriteChannel;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * JBoss GAE File service.
 * <p/>
 * TODO -- do we really need to copy/paste,
 * or can we just be consistent on our own, and it will work?
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossFileService implements FileService {
    static final String PACKAGE = "file";

    static final String FILESYSTEM_BLOBSTORE = AppEngineFile.FileSystem.BLOBSTORE.getName();
    static final String PARAMETER_MIME_TYPE = "content_type";
    static final String PARAMETER_BLOB_INFO_UPLOADED_FILE_NAME = "file_name";
    static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private static final String BLOB_INFO_CREATION_HANDLE_PROPERTY = "creation_handle";

    private static final String CREATION_HANDLE_PREFIX = "writable:";

    private static final String BLOB_FILE_INDEX_KIND = "__BlobFileIndex__";

    private static final String BLOB_KEY_PROPERTY_NAME = "blob_key";

    public AppEngineFile createNewBlobFile(String mimeType) throws IOException {
        return createNewBlobFile(mimeType, "");
    }

    public AppEngineFile createNewBlobFile(String mimeType, String blobInfoUploadedFileName) throws IOException {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            mimeType = DEFAULT_MIME_TYPE;
        }

        Map<String, String> params = new TreeMap<String, String>();
        params.put(PARAMETER_MIME_TYPE, mimeType);
        if (blobInfoUploadedFileName != null && blobInfoUploadedFileName.isEmpty() == false) {
            params.put(PARAMETER_BLOB_INFO_UPLOADED_FILE_NAME, blobInfoUploadedFileName);
        }
        String filePath = create(FILESYSTEM_BLOBSTORE, null, FileServicePb.FileContentType.ContentType.RAW, params);
        AppEngineFile file = new AppEngineFile(filePath);
        if (file.getNamePart().startsWith(CREATION_HANDLE_PREFIX) == false) {
            throw new RuntimeException("Expected creation handle: " + file.getFullPath());
        }
        return file;
    }

    /**
     * Create a file path.
     *
     * @return created file name.
     */
    private String create(
            String fileSystem, String fileName, FileServicePb.FileContentType.ContentType contentType, Map<String, String> parameters)
            throws IOException {

        return fileSystem + "-" + fileName;
    }

    public FileWriteChannel openWriteChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
        GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
        return new JBossFileWriteChannel(gfs.getOutput(file.getFullPath())); // TODO lock?
    }

    public FileReadChannel openReadChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, LockException, IOException {
        GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
        return new JBossFileReadChannel(gfs.getInput(file.getFullPath())); // TODO lock?
    }

    public RecordWriteChannel openRecordWriteChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RecordReadChannel openRecordReadChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, LockException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BlobKey getBlobKey(AppEngineFile file) {
        if (null == file) {
            throw new NullPointerException("file is null");
        }
        if (file.getFileSystem() != AppEngineFile.FileSystem.BLOBSTORE) {
            throw new IllegalArgumentException("file is not of type BLOBSTORE");
        }
        // TODO -- reflection utils
//        BlobKey cached = file.getCachedBlobKey();
//        if (null != cached) {
//            return cached;
//        }
        String namePart = file.getNamePart();
        String creationHandle = (namePart.startsWith(CREATION_HANDLE_PREFIX) ? namePart : null);

        if (creationHandle == null) {
            return new BlobKey(namePart);
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String origNamespace = NamespaceManager.get();
        Query query;
        Entity blobInfoEntity;
        try {
            NamespaceManager.set("");
            try {
                Entity blobFileIndexEntity = datastore.get(KeyFactory.createKey(BLOB_FILE_INDEX_KIND, creationHandle));
                String blobKey = (String) blobFileIndexEntity.getProperty("blob_key");
                blobInfoEntity = datastore.get(KeyFactory.createKey(BlobInfoFactory.KIND, blobKey));
            } catch (EntityNotFoundException ex) {
                query = new Query(BlobInfoFactory.KIND);
                query.addFilter(BLOB_INFO_CREATION_HANDLE_PROPERTY, Query.FilterOperator.EQUAL, creationHandle);
                blobInfoEntity = datastore.prepare(query).asSingleEntity();
            }
        } finally {
            NamespaceManager.set(origNamespace);
        }

        if (blobInfoEntity == null) {
            return null;
        }
        BlobInfo blobInfo = new BlobInfoFactory().createBlobInfo(blobInfoEntity);
        return blobInfo.getBlobKey();
    }

    public AppEngineFile getBlobFile(BlobKey blobKey) throws FileNotFoundException {
        if (blobKey == null) {
            throw new NullPointerException("blobKey is null");
        }
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity;
        try {
            entity = datastore.get(getMetadataKeyForBlobKey(blobKey));
        } catch (EntityNotFoundException ex) {
            throw new FileNotFoundException();
        }
        String creationHandle = (String) entity.getProperty(BLOB_INFO_CREATION_HANDLE_PROPERTY);
        String namePart = (creationHandle == null ? blobKey.getKeyString() : creationHandle);
        AppEngineFile file = new AppEngineFile(AppEngineFile.FileSystem.BLOBSTORE, namePart);
        // file.setCachedBlobKey(blobKey); // TODO
        return file;
    }

    protected Key getMetadataKeyForBlobKey(BlobKey blobKey) {
        String origNamespace = NamespaceManager.get();
        try {
            NamespaceManager.set("");
            return KeyFactory.createKey(null, BlobInfoFactory.KIND, blobKey.getKeyString());
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }
}
