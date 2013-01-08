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

package org.jboss.capedwarf.common.util;

import java.util.concurrent.Future;

/**
 * Simple utils.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class Util {
    private Util() {
    }

    /**
     * Check value for null, return default if true.
     *
     * @param value the value to check
     * @param defaultValue the default value
     * @return value if not null, default otherwise
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return (value != null) ? value : defaultValue;
    }

    /**
     * Quiet future get.
     * Wrap exception into runtime exception.
     *
     * @param future the future
     * @return future's get result
     */
    public static <R> R quietGet(Future<R> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
    }
}
