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

package org.jboss.test.capedwarf.images.util;

import org.jboss.capedwarf.images.util.ColorUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ColorUtilsTestCase {

    @Test
    public void testToRGBA() {
        assertEquals(0x000000FFL, ColorUtils.toRGBA(0xFF000000L));
        assertEquals(0xFF000000L, ColorUtils.toRGBA(0x00FF0000L));
        assertEquals(0x00FF0000L, ColorUtils.toRGBA(0x0000FF00L));
        assertEquals(0x0000FF00L, ColorUtils.toRGBA(0x000000FFL));

        assertEquals(0x6699CC33L, ColorUtils.toRGBA(0x336699CCL));
    }

    @Test
    public void testToIntArray() {
        assertArrayEquals(new int[]{0xFF, 0, 0, 0}, ColorUtils.toIntArray(0xFF000000L));
        assertArrayEquals(new int[]{0, 0xFF, 0, 0}, ColorUtils.toIntArray(0x00FF0000L));
        assertArrayEquals(new int[]{0, 0, 0xFF, 0}, ColorUtils.toIntArray(0x0000FF00L));
        assertArrayEquals(new int[]{0, 0, 0, 0xFF}, ColorUtils.toIntArray(0x000000FFL));
    }


    private void assertEquals(long expected, long actual) {
        if (expected != actual) {
            fail("Expected " + Long.toHexString(expected) + "; was: " + Long.toHexString(actual));
        }
    }
}
