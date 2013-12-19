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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileStat;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.api.files.RecordReadChannel;
import com.google.appengine.api.files.RecordWriteChannel;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.io.DigestResult;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * JBoss GAE File service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@SuppressWarnings("deprecation")
public class CapedwarfFileService implements ExposedFileService {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final String KIND_TEMP_BLOB_INFO = "__BlobInfo_temp__";
    private static final String MD5_HASH_STATE = BlobInfoFactory.MD5_HASH + "__State";

    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    private BlobInfoFactory blobInfoFactory;

    private synchronized BlobInfoFactory getBlobInfoFactory() {
        if (blobInfoFactory == null) {
            blobInfoFactory = new BlobInfoFactory(datastoreService);
        }
        return blobInfoFactory;
    }

    public BlobInfo getBlobInfo(BlobKey key) {
        return getBlobInfoFactory().loadBlobInfo(key);
    }

    public FileInfo getFileInfo(BlobKey key) {
        Entity info = getFileInfo(getFileInfoKey(key));
        if (info != null) {
            String contentType = (String) info.getProperty(BlobInfoFactory.CONTENT_TYPE);
            Date creation = (Date) info.getProperty(BlobInfoFactory.CREATION);
            String filename = (String) info.getProperty(BlobInfoFactory.FILENAME);
            Object sp = info.getProperty(BlobInfoFactory.SIZE);
            long size = (sp != null ? (Long) sp : -1);
            String md5Hash = (String) info.getProperty(BlobInfoFactory.MD5_HASH);
            String gsObjectName = null; // TODO
            return new FileInfo(contentType, creation, filename, size, md5Hash, gsObjectName);
        } else {
            return null;
        }
    }

    public AppEngineFile createNewBlobFile(String mimeType) throws IOException {
        return createNewBlobFile(mimeType, "");
    }

    public AppEngineFile createNewBlobFile(String contentType, String uploadedFileName) throws IOException {
        return createNewBlobFile(contentType, null, uploadedFileName, AppEngineFile.FileSystem.BLOBSTORE);
    }

    public AppEngineFile createNewGSFile(GSFileOptions options) throws IOException {
        final GSFileOptionsAdapter adapter = new GSFileOptionsAdapter(options);
        final String fileName = adapter.getFileName();
        return createNewBlobFile(adapter.getMimeType(), fileName, fileName, AppEngineFile.FileSystem.GS);
    }

    public void delete(BlobKey... blobKeys) {
        GridFilesystem gfs = getGridFilesystem();
        for (BlobKey key : blobKeys) {
            File file = gfs.getFile(getFilePath(key));
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public InputStream getStream(BlobKey blobKey) throws FileNotFoundException {
        GridFilesystem gfs = getGridFilesystem();
        return gfs.getInput(getFilePath(blobKey));
    }

    public FileWriteChannel openWriteChannel(AppEngineFile file, boolean lock) throws IOException {
        if (isFinalized(file)) {
            throwFinalizationException();
        }

        createBlobstoreDirIfNeeded(file);

        GridFilesystem gfs = getGridFilesystem();
        return new CapedwarfFileWriteChannel(file, gfs.getWritableChannel(getFilePath(file), true), this, lock);
    }

    public FileReadChannel openReadChannel(AppEngineFile file, boolean lock) throws IOException {
        if (!exists(file)) {
            throw new FileNotFoundException("File " + file + " not found.");
        }
        if (!isFinalized(file)) {
            throwFinalizationException();
        }
        GridFilesystem gfs = getGridFilesystem();
        return new CapedwarfFileReadChannel(gfs.getReadableChannel(getFilePath(file)));
    }

    public String getDefaultGsBucketName() {
        return String.format("%s.appspot.com", Application.getAppId()); // OK?
    }

    @Override
    public boolean exists(AppEngineFile file) {
        return getGfsFile(file).exists();
    }

    public RecordWriteChannel openRecordWriteChannel(AppEngineFile file, boolean lock) throws IOException {
        return ReflectionUtils.newInstance(
                "com.google.appengine.api.files.RecordWriteChannelImpl",
                new Class[]{FileWriteChannel.class},
                new Object[]{openWriteChannel(file, lock)});
    }

    public RecordReadChannel openRecordReadChannel(AppEngineFile file, boolean lock) throws IOException {
        return ReflectionUtils.newInstance(
                "com.google.appengine.api.files.RecordReadChannelImpl",
                new Class[]{FileReadChannel.class},
                new Object[]{openReadChannel(file, lock)});
    }

    public BlobKey getBlobKey(AppEngineFile file) {
        AppEngineFileAdapter adapter = new AppEngineFileAdapter(file);
        return adapter.getBlobKey();
    }

    public AppEngineFile getBlobFile(BlobKey blobKey) {
        return new AppEngineFile(AppEngineFile.FileSystem.BLOBSTORE, blobKey.getKeyString());
    }

    public FileStat stat(AppEngineFile file) throws IOException {
        Entity info = getFileInfo(getFileInfoKey(getBlobKey(file)));
        if (info == null) {
            info = getFileInfo(getTempBlobInfoKey(file));
            if (info == null) {
                throw new FileNotFoundException(file.toString());
            } else {
                throw ReflectionUtils.newInstance(FinalizationException.class);
            }
        } else {
            final FileStat stat = new FileStat();
            stat.setFinalized(true);
            stat.setFilename(file.getFullPath());
            stat.setLength((Long) info.getProperty(BlobInfoFactory.SIZE));
            // TODO -- setMtime, setCtime
            return stat;
        }
    }

    public void delete(AppEngineFile... appEngineFiles) throws IOException {
        final Set<AppEngineFile> failed = new HashSet<AppEngineFile>();
        for (AppEngineFile aef : appEngineFiles) {
            if (getGfsFile(aef).delete() == false) {
                failed.add(aef);
            }
        }
        if (failed.isEmpty() == false)
            throw new IOException("Failed to delete files: " + failed);
    }

    private Key getTempBlobInfoKey(AppEngineFile file) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return KeyFactory.createKey(KIND_TEMP_BLOB_INFO, file.getFullPath());
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private Key getFileInfoKey(BlobKey key) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return KeyFactory.createKey(BlobInfoFactory.KIND, key.getKeyString());
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private AppEngineFile createNewBlobFile(String contentType, String fileName, String uploadedFileName, AppEngineFile.FileSystem fs) throws IOException {
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = DEFAULT_MIME_TYPE;
        }

        AppEngineFile file;
        if (fileName == null) {
            fileName = generateUniqueFileName();
            file = new AppEngineFile(fs, fileName);
        } else {
            file = new AppEngineFile(fileName);
        }

        storeTemporaryBlobInfo(file, contentType, new Date(), uploadedFileName);

        return file;
    }

    private void storeTemporaryBlobInfo(AppEngineFile file, String contentType, Date creationTimestamp, String uploadedFileName) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Entity tempBlobInfo = new Entity(getTempBlobInfoKey(file));
            tempBlobInfo.setProperty(BlobInfoFactory.CONTENT_TYPE, contentType);
            tempBlobInfo.setProperty(BlobInfoFactory.CREATION, creationTimestamp);
            tempBlobInfo.setProperty(BlobInfoFactory.FILENAME, uploadedFileName);
            datastoreService.put(tempBlobInfo);
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private Entity getTemporaryInfo(AppEngineFile file) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            return datastoreService.get(getTempBlobInfoKey(file));
        } catch (EntityNotFoundException e) {
            throw new IllegalStateException("Cannot finalize file " + file + ". Cannot find temp blob info.");
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    void saveMd5(AppEngineFile file, DigestResult dg) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Entity info = getTemporaryInfo(file);
            byte[] bytes = dg.getState();
            if (bytes != null) {
                info.setProperty(MD5_HASH_STATE, new Blob(bytes));
            }
            info.setProperty(BlobInfoFactory.MD5_HASH, dg.getDigest());
            datastoreService.put(info);
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    DigestResult readMd5(AppEngineFile file) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Entity info = getTemporaryInfo(file);
            Blob state = (Blob) info.getProperty(MD5_HASH_STATE);
            String md5 = (String) info.getProperty(BlobInfoFactory.MD5_HASH);
            return new DigestResult(state != null ? state.getBytes() : null, md5);
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    void finalizeFile(AppEngineFile file) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Entity tempBlobInfo = getTemporaryInfo(file);

            File gfsFile = getGfsFile(file);
            if (gfsFile.exists() == false) {
                throw new IllegalStateException("Cannot finalize file " + file + ". Cannot find file on grid filesystem.");
            }

            String blobKeyString = getBlobKey(file).getKeyString();
            Entity blobInfo = new Entity(BlobInfoFactory.KIND, blobKeyString);
            blobInfo.setProperty(BlobInfoFactory.CONTENT_TYPE, tempBlobInfo.getProperty(BlobInfoFactory.CONTENT_TYPE));
            blobInfo.setProperty(BlobInfoFactory.CREATION, tempBlobInfo.getProperty(BlobInfoFactory.CREATION));
            blobInfo.setProperty(BlobInfoFactory.FILENAME, tempBlobInfo.getProperty(BlobInfoFactory.FILENAME));
            blobInfo.setProperty(BlobInfoFactory.MD5_HASH, tempBlobInfo.getProperty(BlobInfoFactory.MD5_HASH));
            blobInfo.setProperty(BlobInfoFactory.SIZE, gfsFile.length());
            datastoreService.put(blobInfo);
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }

    private File getGfsFile(AppEngineFile file) {
        GridFilesystem gfs = getGridFilesystem();
        return gfs.getFile(getFilePath(file));
    }

    private String generateUniqueFileName() {
        return UUID.randomUUID().toString();
    }

    private void throwFinalizationException() throws FinalizationException {
        throw ReflectionUtils.newInstance(FinalizationException.class);
    }

    private boolean isFinalized(AppEngineFile file) {
        BlobKey blobKey = getBlobKey(file);
        BlobInfo blobInfo = getBlobInfo(blobKey);
        return blobInfo != null;
    }

    private void createBlobstoreDirIfNeeded(AppEngineFile file) {
        // TODO: this is temporary
        String fullPath = file.getFullPath();
        int p = fullPath.lastIndexOf("/");
        String dirs = fullPath.substring(0, p);
        //noinspection ResultOfMethodCallIgnored
        getGridFilesystem().getFile(dirs).mkdirs();
    }

    private String getFilePath(AppEngineFile file) {
        return removeLeadingSeparator(file.getFullPath());
    }

    private String getFilePath(BlobKey blobKey) {
        return getFilePath(getBlobFile(blobKey));
    }

    private String removeLeadingSeparator(String fullPath) {
        return fullPath.startsWith("/") ? fullPath.substring(1) : fullPath; // TODO: fix grid FS and remove this method
    }

    private GridFilesystem getGridFilesystem() {
        return InfinispanUtils.getGridFilesystem(Application.getAppId());
    }

    protected Entity getFileInfo(Key key) {
        final String origNamespace = NamespaceManager.get();
        NamespaceManager.set("");
        try {
            Map<Key, Entity> map = datastoreService.get(Collections.singleton(key));
            return (map.isEmpty() ? null : map.values().iterator().next());
        } finally {
            NamespaceManager.set(origNamespace);
        }
    }
}
