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
import org.jboss.capedwarf.shared.reflection.MethodInvocation;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class AppEngineFileAdapter {
    private final static MethodInvocation<BlobKey> getCachedBlobKey = ReflectionUtils.cacheMethod(AppEngineFile.class, "getCachedBlobKey");
    private final static MethodInvocation<Void> setCachedBlobKey = ReflectionUtils.cacheMethod(AppEngineFile.class, "setCachedBlobKey", BlobKey.class);

    private AppEngineFile file;

    public AppEngineFileAdapter(AppEngineFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        this.file = file;
    }

    public String getNamePart() {
        return file.getNamePart();
    }

    public String getFullPath() {
        return file.getFullPath();
    }

    public BlobKey getBlobKey() {
        BlobKey cached = getCachedBlobKey();
        if (cached != null) {
            return cached;
        }

        cached = new BlobKey(getFilePath(file));
        setCachedBlobKey(cached);
        return cached;
    }

    BlobKey getCachedBlobKey() {
        return getCachedBlobKey.invokeWithTarget(file);
    }

    void setCachedBlobKey(BlobKey blobKey) {
        setCachedBlobKey(file, blobKey);
    }

    static void setCachedBlobKey(AppEngineFile file, BlobKey blobKey) {
        setCachedBlobKey.invokeWithTarget(file, blobKey);
    }

    static String getFilePath(AppEngineFile file) {
        return removeLeadingSeparator(file.getFullPath());
    }

    private static String removeLeadingSeparator(String fullPath) {
        return fullPath.startsWith("/") ? fullPath.substring(1) : fullPath; // TODO: fix grid FS and remove this method
    }
}
