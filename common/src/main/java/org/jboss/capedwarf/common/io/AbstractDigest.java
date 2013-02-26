/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
abstract class AbstractDigest implements Digest {
    protected final Logger log = Logger.getLogger(getClass().getName());
    private static final char[] Hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String toHexString(byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];
        for (int b = 0, c = 0; b < bytes.length; b++) {
            int v = (int) bytes[b] & 0xFF;
            chars[c++] = Hexadecimal[v / 16];
            chars[c++] = Hexadecimal[v % 16];
        }
        return new String(chars);
    }

    public DigestResult digest() {
        return new DigestResult(dump(), toHexString(internalDigest()));
    }

    protected abstract byte[] dump();
    protected abstract byte[] internalDigest();
}
