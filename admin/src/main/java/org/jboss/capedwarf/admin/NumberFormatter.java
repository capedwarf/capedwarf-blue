/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.admin;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class NumberFormatter {
    public static final long K = 1000;

    public static String formatCount(long count) {
        return String.valueOf(count);
    }

    public static String formatBytes(long bytes) {
        if (bytes < K) {
            return bytes + " " + singularOrPlural("Byte", bytes);
        } else if (bytes < K * K) {
            long kbytes = bytes / K;
            return kbytes + " " + singularOrPlural("KByte", kbytes);
        } else {
            long mbytes = bytes / (K * K);
            return mbytes + " " + singularOrPlural("MByte", mbytes);
        }
    }

    private static String singularOrPlural(String unit, long count) {
        return count == 1 ? unit : (unit + "s");
    }
}
