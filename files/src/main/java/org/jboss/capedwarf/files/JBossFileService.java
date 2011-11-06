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
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.files.*;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.TreeMap;

/**
 * JBoss GAE File service.
 * <p/>
 * TODO -- do we really need to copy/paste,
 * or can we just be consistent on our own, and it will work?
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
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

    public AppEngineFile createNewBlobFile(String mimeType, String uploadedFileName) throws IOException {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            mimeType = DEFAULT_MIME_TYPE;
        }

        Map<String, String> params = new TreeMap<String, String>();
        params.put(PARAMETER_MIME_TYPE, mimeType);
        if (uploadedFileName != null && !uploadedFileName.isEmpty()) {
            params.put(PARAMETER_BLOB_INFO_UPLOADED_FILE_NAME, uploadedFileName);
        }
        String filePath = createFilePath(FILESYSTEM_BLOBSTORE, null, FileServicePb.FileContentType.ContentType.RAW, params);
        AppEngineFile file = new AppEngineFile(filePath);
        if (!file.getNamePart().startsWith(CREATION_HANDLE_PREFIX)) {
            throw new RuntimeException("Expected creation handle: " + file.getFullPath());
        }
        return file;
    }

    public AppEngineFile createNewBlobFile(String mimeType, String uploadedFileName, ReadableByteChannel in) throws IOException {
        AppEngineFile file = createNewBlobFile(mimeType, uploadedFileName);
        writeTo(file, in);
        return file;
    }

    private void writeTo(AppEngineFile file, ReadableByteChannel in) throws IOException {
        FileWriteChannel out = openWriteChannel(file, true);
        try {
            IOUtils.copy(in, out);
        } finally {
            out.closeFinally();
        }
    }

    private String createFilePath(
            String fileSystem, String fileName, FileServicePb.FileContentType.ContentType contentType, Map<String, String> parameters)
            throws IOException {

        return fileSystem + "-" + fileName;
    }

    public void delete(BlobKey... blobKeys) {
        GridFilesystem gfs = getGridFilesystem();
        for (BlobKey key : blobKeys)
            gfs.remove(key.getKeyString(), true);
    }

    public InputStream getStream(BlobKey blobKey) throws FileNotFoundException {
        GridFilesystem gfs = getGridFilesystem();
        return gfs.getInput(blobKey.getKeyString());
    }

    public FileWriteChannel openWriteChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
        GridFilesystem gfs = getGridFilesystem();
        return new JBossFileWriteChannel(gfs.getOutput(file.getFullPath())); // TODO lock?
    }

    public FileReadChannel openReadChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, LockException, IOException {
        GridFilesystem gfs = getGridFilesystem();
        return new JBossFileReadChannel(gfs.getInput(file.getFullPath())); // TODO lock?
    }

    private GridFilesystem getGridFilesystem() {
        return InfinispanUtils.getGridFilesystem();
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

        BlobKey cached = getCachedBlobKey(file);
        if (null != cached) {
            return cached;
        }

        String creationHandle = getCreationHandle(file);
        if (creationHandle == null) {
            return new BlobKey(file.getNamePart());
        }

        Entity blobInfoEntity = getBlobInfoEntity(creationHandle);
        if (blobInfoEntity == null) {
            return null;
        }

        BlobInfo blobInfo = new BlobInfoFactory().createBlobInfo(blobInfoEntity);
        return blobInfo.getBlobKey();
    }

    private String getCreationHandle(AppEngineFile file) {
        String namePart = file.getNamePart();
        return (namePart.startsWith(CREATION_HANDLE_PREFIX) ? namePart : null);
    }

    private Entity getBlobInfoEntity(String creationHandle) {
        String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            try {
                return getBlobInfoEntityDirectly(creationHandle);
            } catch (EntityNotFoundException ex) {
                return getBlobInfoEntityThroughQuery(creationHandle);
            }
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private Entity getBlobInfoEntityDirectly(String creationHandle) throws EntityNotFoundException {
        Entity blobFileIndexEntity = getBlobFileIndexEntity(creationHandle);
        String blobKey = getBlobKey(blobFileIndexEntity);

        return getDatastoreService().get(KeyFactory.createKey(BlobInfoFactory.KIND, blobKey));
    }

    private Entity getBlobFileIndexEntity(String creationHandle) throws EntityNotFoundException {
        DatastoreService datastore = getDatastoreService();
        return datastore.get(KeyFactory.createKey(BLOB_FILE_INDEX_KIND, creationHandle));
    }

    private String getBlobKey(Entity blobFileIndexEntity) {
        return (String) blobFileIndexEntity.getProperty(BLOB_KEY_PROPERTY_NAME);
    }

    private Entity getBlobInfoEntityThroughQuery(String creationHandle) {
        Query query = new Query(BlobInfoFactory.KIND);
        query.addFilter(BLOB_INFO_CREATION_HANDLE_PROPERTY, Query.FilterOperator.EQUAL, creationHandle);
        return getDatastoreService().prepare(query).asSingleEntity();
    }

    private DatastoreService getDatastoreService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    public AppEngineFile getBlobFile(BlobKey blobKey) throws FileNotFoundException {
        if (blobKey == null) {
            throw new NullPointerException("blobKey is null");
        }
        Entity entity = getEntity(blobKey);
        String creationHandle = (String) entity.getProperty(BLOB_INFO_CREATION_HANDLE_PROPERTY);
        String namePart = (creationHandle == null ? blobKey.getKeyString() : creationHandle);
        AppEngineFile file = new AppEngineFile(AppEngineFile.FileSystem.BLOBSTORE, namePart);
        setCachedBlobKey(file, blobKey);
        return file;
    }

    private Entity getEntity(BlobKey blobKey) throws FileNotFoundException {
        DatastoreService datastore = getDatastoreService();
        try {
            return datastore.get(getMetadataKeyForBlobKey(blobKey));
        } catch (EntityNotFoundException ex) {
            throw new FileNotFoundException();
        }
    }

    protected Key getMetadataKeyForBlobKey(BlobKey blobKey) {
        String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return KeyFactory.createKey(null, BlobInfoFactory.KIND, blobKey.getKeyString());
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private BlobKey getCachedBlobKey(AppEngineFile file) {
        return (BlobKey) ReflectionUtils.invokeInstanceMethod(file, "getCachedBlobKey");
    }

    private void setCachedBlobKey(AppEngineFile file, BlobKey blobKey) {
        ReflectionUtils.invokeInstanceMethod(file, "setCachedBlobKey", BlobKey.class, blobKey);
    }
}
