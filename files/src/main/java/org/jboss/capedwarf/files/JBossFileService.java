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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.api.files.LockException;
import com.google.appengine.api.files.RecordReadChannel;
import com.google.appengine.api.files.RecordWriteChannel;
import com.google.appengine.api.files.RecordWriteChannelImpl;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * JBoss GAE File service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossFileService implements FileService {

    static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public AppEngineFile createNewBlobFile(String mimeType) throws IOException {
        return createNewBlobFile(mimeType, "");
    }

    public AppEngineFile createNewBlobFile(String mimeType, String uploadedFileName) throws IOException {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            mimeType = DEFAULT_MIME_TYPE;
        }

        String fileName = generateUniqueFileName();
        AppEngineFile file = new AppEngineFile(AppEngineFile.FileSystem.BLOBSTORE, fileName);
//        String blobKeyString = file.getFullPath();
//        new BlobInfo(new BlobKey(blobKeyString), mimeType, new Date(), uploadedFileName, 0);    // TODO: size
//        new BlobInfoFactory().loadBlobInfo(new BlobKey(blobKeyString))
        return file;
    }

    private String generateUniqueFileName() {
        return Long.toHexString(new Random().nextLong());   // TODO
    }

    public AppEngineFile createNewGSFile(GSFileOptions gsFileOptions) throws IOException {
        return null; // TODO
    }

    public void delete(BlobKey... blobKeys) {
        GridFilesystem gfs = getGridFilesystem();
        for (BlobKey key : blobKeys)
            gfs.remove(removeLeadingSeparator(key.getKeyString()), true);
    }

    public InputStream getStream(BlobKey blobKey) throws FileNotFoundException {
        GridFilesystem gfs = getGridFilesystem();
        return gfs.getInput(removeLeadingSeparator(blobKey.getKeyString()));
    }

    public FileWriteChannel openWriteChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
        if (isFinalized(file)) {
            throwFinalizationException();
        }
        GridFilesystem gfs = getGridFilesystem();
        String pathName = removeLeadingSeparator(file.getFullPath());
        createBlobstoreDirIfNeeded();
        return new JBossFileWriteChannel(gfs.getWritableChannel(pathName, true), lock);
    }

    private void throwFinalizationException() throws FinalizationException {
        throw ReflectionUtils.newInstance(FinalizationException.class);
    }

    private boolean isFinalized(AppEngineFile file) {
        return false;
    }

    private void createBlobstoreDirIfNeeded() {
        getGridFilesystem().getFile("blobstore").mkdirs();  // TODO: this is temporary
    }

    public FileReadChannel openReadChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, LockException, IOException {
        if (!isFinalized(file)) {
            throwFinalizationException();
        }
        GridFilesystem gfs = getGridFilesystem();
        String pathName = removeLeadingSeparator(file.getFullPath());
        return new JBossFileReadChannel(gfs.getReadableChannel(pathName));
    }

    private String removeLeadingSeparator(String fullPath) {
        return fullPath.startsWith("/") ? fullPath.substring(1) : fullPath; // TODO: fix grid FS and remove this method
    }

    private GridFilesystem getGridFilesystem() {
        return InfinispanUtils.getGridFilesystem();
    }

    public RecordWriteChannel openRecordWriteChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, FinalizationException, LockException, IOException {
        return new RecordWriteChannelImpl(openWriteChannel(file, lock));
    }

    public RecordReadChannel openRecordReadChannel(AppEngineFile file, boolean lock) throws FileNotFoundException, LockException, IOException {
        return ReflectionUtils.newInstance(
                "com.google.appengine.api.files.RecordReadChannelImpl",
                new Class[]{FileReadChannel.class},
                new Object[]{openReadChannel(file, lock)});
    }

    public BlobKey getBlobKey(AppEngineFile file) {
        AppEngineFileAdapter adapter = new AppEngineFileAdapter(file);
        return adapter.getBlobKey();
    }

    public AppEngineFile getBlobFile(BlobKey blobKey) throws FileNotFoundException {
        return new AppEngineFile(blobKey.getKeyString());
    }

}
