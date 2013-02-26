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

import java.nio.ByteBuffer;

import org.bouncycastle.crypto.digests.MD5Digest;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class BCMD5Digest extends AbstractDigest {
    private MD5Digest digest;
    private byte[] state;

    private synchronized MD5Digest getDigest() {
        if (digest == null) {
            if (state == null || state.length == 0) {
                digest = new MD5Digest();
            } else {
                digest = new MD5Digest(MarshallingUtils.INTERNAL.readObject(MD5Digest.class, state));
            }
        }
        return digest;
    }

    protected byte[] internalDigest() {
        byte[] bytes = new byte[getDigest().getDigestSize()];
        getDigest().doFinal(bytes, 0);
        return bytes;
    }

    protected synchronized byte[] dump() {
        if (digest == null) {
            // no update yet
            return state;
        } else {
            return MarshallingUtils.INTERNAL.writeObject(getDigest());
        }
    }

    public void initialize(DigestResult previous) {
        if (previous != null) {
            state = previous.getState();
        }
    }

    public void update(byte[] bytes) {
        getDigest().update(bytes, 0, bytes.length);
    }

    public void update(ByteBuffer buffer) {
        update(buffer.array());
    }
}
