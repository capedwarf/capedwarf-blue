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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileWriteChannel;
import org.infinispan.io.WritableGridFileChannel;
import org.jboss.capedwarf.common.io.Digest;
import org.jboss.capedwarf.common.io.DigestResult;
import org.jboss.capedwarf.common.io.IOUtils;

/**
 * JBoss file write channel.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class CapedwarfFileWriteChannel implements FileWriteChannel {
    private AppEngineFile file;
    private final WritableGridFileChannel delegate;
    private CapedwarfFileService fileService;
    private boolean lockHeld;

    private volatile Digest digest;

    CapedwarfFileWriteChannel(AppEngineFile file, WritableGridFileChannel channel, CapedwarfFileService fileService, boolean lock) {
        this.file = file;
        this.delegate = channel;
        this.fileService = fileService;
        this.lockHeld = lock;
    }

    public int write(ByteBuffer buffer, String sequenceKey) throws IOException {
        return write(buffer);  // TODO
    }

    public int write(ByteBuffer buffer) throws IOException {
        int write = delegate.write(buffer);
        getDigest().update(buffer);
        return write;
    }

    public boolean isOpen() {
        return delegate.isOpen();
    }

    public void close() throws IOException {
        try {
            Digest md = getDigest();
            digest = null;
            fileService.saveMd5(file, md.digest());
        } finally {
            delegate.close();
        }
    }

    public void closeFinally() throws IllegalStateException, IOException {
        if (!lockHeld) {
            throw new IllegalStateException("The lock for this file is not held by the current request");
        }
        close();
        fileService.finalizeFile(file);
    }

    protected synchronized Digest getDigest() {
        if (digest == null) {
            try {
                digest = IOUtils.getDigest("MD5");
                DigestResult previousResult = fileService.readMd5(file);
                digest.initialize(previousResult);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return digest;
    }
}
