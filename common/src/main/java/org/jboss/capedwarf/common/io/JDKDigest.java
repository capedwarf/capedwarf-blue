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
import java.security.MessageDigest;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class JDKDigest extends AbstractDigest {
    private final MessageDigest digest;

    JDKDigest(String algorithm) throws Exception {
        this.digest = MessageDigest.getInstance(algorithm);
    }

    protected byte[] internalDigest() {
        return digest.digest();
    }

    protected byte[] dump() {
        throw new UnsupportedOperationException("Dump not supported for digest: " + digest);
    }

    public void initialize(DigestResult previous) {
        throw new UnsupportedOperationException("Initialization not supported for digest: " + digest);
    }

    public void update(byte[] bytes) {
        digest.update(bytes);
    }

    public void update(ByteBuffer buffer) {
        digest.update(buffer);
    }
}
