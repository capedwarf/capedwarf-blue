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

package org.jboss.capedwarf.datastore;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class PropertyUtils {
    private static final String UNINDEXED_VALUE_CLASS_NAME = Entity.class.getName() + "$UnindexedValue";
    private static final Set<String> SPECIAL_PROPERTIES;

    static {
        SPECIAL_PROPERTIES = new HashSet<String>();
        SPECIAL_PROPERTIES.add(Entity.KEY_RESERVED_PROPERTY);
        SPECIAL_PROPERTIES.add(Entity.VERSION_RESERVED_PROPERTY);
        SPECIAL_PROPERTIES.add(Entity.SCATTER_RESERVED_PROPERTY);
    }

    private PropertyUtils() {
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isIndexedProperty(Object value) {
        if (value == null)
            return true;

        if (value instanceof Text) {
            return false;
        } else if (value instanceof Blob) {
            return false;
        } else if (value instanceof EmbeddedEntity) {
            return false;
        } else {
            return UNINDEXED_VALUE_CLASS_NAME.equals(value.getClass().getName()) == false;
        }
    }

    public static boolean isSpecialProperty(String propertyName) {
        return SPECIAL_PROPERTIES.contains(propertyName);
    }
}
