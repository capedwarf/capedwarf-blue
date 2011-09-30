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

package org.jboss.capedwarf.images.util;

import java.awt.*;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ColorUtils {

    public static long toRGBA(long argb) {
        long rgbPart = (argb << 8) & 0xFFFFFF00L;
        long aPart = (argb >> 24) & 0xFFL;
        return aPart | rgbPart;
    }

    public static int[] toIntArray(long color) {
        return new int[]{
                0xFF & (int) (color >> 24),
                0xFF & (int) (color >> 16),
                0xFF & (int) (color >> 8),
                0xFF & (int) (color)
        };
    }

    public static Color fromLongARGB(long argb) {
        int[] ints = toIntArray(argb);
        int a = ints[0];
        int r = ints[1];
        int g = ints[2];
        int b = ints[3];
        return new Color(r, g, b, a);
    }

    public static int[] toRGBAIntArray(long argb) {
        return toIntArray(toRGBA(argb));
    }
}
