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

import com.google.appengine.api.files.FileReadChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

/**
 * JBoss file read channel.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class JBossFileReadChannel implements FileReadChannel {
    private final ReadableByteChannel delegate;
    private long position;

    JBossFileReadChannel(InputStream in) {
        delegate = Channels.newChannel(in);
    }

    protected void checkOpen() throws ClosedChannelException {
        if (isOpen() == false) {
            throw new ClosedChannelException();
        }
    }

    public long position() throws IOException {
        checkOpen();
        return position;
    }

    public FileReadChannel position(long newPosition) throws IOException {
        if (newPosition < 0) {
            throw new IllegalArgumentException("newPosition may not be negative");
        }
        checkOpen();
        position = newPosition;
        // TODO -- move position
        return this;
    }

    public int read(ByteBuffer dst) throws IOException {
        return delegate.read(dst);
    }

    public boolean isOpen() {
        return delegate.isOpen();
    }

    public void close() throws IOException {
        delegate.close();
    }
}
