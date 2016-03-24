/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import org.apache.commons.codec.binary.Base64;

/**
 * Gather all Base64 methods here.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("deprecation")
public class Base64Utils {
    public static byte[] decode(String input) {
        return Base64.decodeBase64(input);
    }

    public static String encode(byte[] input) {
        return Base64.encodeBase64String(input);
    }

    public static byte[] decodeWebSafe(String input) {
        return new Base64(true).decode(input);
    }

    public static String encodeWebSafe(byte[] bytes) {
        return new Base64(true).encodeAsString(bytes);
    }
}
