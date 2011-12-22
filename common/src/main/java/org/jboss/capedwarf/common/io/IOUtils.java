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

package org.jboss.capedwarf.common.io;

import com.google.appengine.api.files.FileWriteChannel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class IOUtils {
    /**
     * Safe close.
     *
     * @param closeable the closeable resource
     */
    public static void safeClose(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
    }

    /**
     * Get bytes from input stream.
     *
     * @param is          the input stream
     * @param closeStream should we close the stream
     * @return bytes
     * @throws IOException for any IO error
     */
    public static byte[] toBytes(InputStream is, boolean closeStream) throws IOException {
        return toBytes(is, 0, Long.MAX_VALUE, closeStream);
    }

    /**
     * Get bytes from input stream.
     *
     * @param is          the input stream
     * @param start       the start
     * @param end         the end
     * @param closeStream should we close the stream
     * @return bytes
     * @throws IOException for any IO error
     */
    public static byte[] toBytes(InputStream is, long start, long end, boolean closeStream) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1 && end > 0) {
                if (start > 0)
                    continue;

                baos.write(b);
                start--;
                end--;
            }
            return baos.toByteArray();
        } finally {
            if (closeStream)
                safeClose(is);
        }
    }

    /**
     * Copy stream.
     *
     * @param in  the input stream
     * @param out the output stream
     * @throws IOException for any IO error
     */
    public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }

    /**
     * Copy stream.
     *
     * @param in     the input stream
     * @param out    the output stream
     * @param offset how many input bytes should be skipped
     * @throws IOException for any IO error
     */
    public static void copyStream(final InputStream in, final OutputStream out, long offset) throws IOException {
        skip(in, offset);
        copyStream(in, out);
    }

    /**
     * Copy stream.
     *
     * @param in     the input stream
     * @param out    the output stream
     * @param offset how many input bytes should be skipped
     * @param length how many bytes should be copied
     * @throws IOException for any IO error
     */
    public static void copyStream(final InputStream in, final OutputStream out, long offset, long length) throws IOException {
        skip(in, offset);
        copyStreamBounded(in, out, length);
    }

    public static void skip(InputStream stream, long count) throws IOException {
        while (count > 0) {
            count -= stream.skip(count);
        }
    }

    public static void copyStreamBounded(InputStream in, OutputStream out, long length) throws IOException {
        final byte[] bytes = new byte[8192];
        long totalCount = 0;
        while (totalCount < length) {
            int len = (int) Math.min(bytes.length, length - totalCount);
            int count = in.read(bytes, 0, len);
            if (count == -1) {
                throw new EOFException("Reached end of stream prematurely.");
            }
            out.write(bytes, 0, count);
            totalCount += count;
        }
    }

    public static void copy(ReadableByteChannel in, FileWriteChannel out) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        while (in.read(buffer) != -1) {
            buffer.flip(); // Prepare the buffer to be drained
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
            buffer.clear(); // Empty buffer to get ready for filling
        }
    }
}
