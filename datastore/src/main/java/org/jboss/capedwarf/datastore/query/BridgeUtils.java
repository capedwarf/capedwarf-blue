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

package org.jboss.capedwarf.datastore.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class BridgeUtils {
    private static final Map<Class<?>, Bridge> MAP;

    static {
        MAP = new HashMap<Class<?>, Bridge>();
        for (Bridge bridge : Bridge.values()) {
            Set<Class<?>> types = bridge.bridge.types();
            if (types != null) {
                for (Class<?> type : types) {
                    MAP.put(type, bridge);
                }
            }
        }
    }

    private BridgeUtils() {
    }

    public static Bridge getBridge(Class<?> type) {
        Bridge bridge = MAP.get(type);
        if (bridge == null) {
            throw new IllegalArgumentException("No bridge found for type " + type);
        } else {
            return bridge;
        }
    }

    public static Bridge matchBridge(Object value) {
        if (value == null) {
            return Bridge.NULL;
        }

        if (value instanceof Collection) {
            return Bridge.COLLECTION;
        } else {
            return getBridge(value.getClass());
        }
    }

}