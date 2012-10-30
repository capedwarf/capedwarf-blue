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

package org.jboss.capedwarf.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class EntityModifierImpl implements EntityModifier {
    static EntityModifier INSTANCE  = new EntityModifierImpl();

    private EntityModifierImpl() {
    }

    public Entity modify(Entity original) {
        final Entity clone = original.clone();
        final Map<String, Object> properties = original.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            final String property = entry.getKey();
            final Object value = entry.getValue();
            boolean unindexed = original.isUnindexedProperty(property);
            modifyProperty(clone, property, value, unindexed);
        }
        return clone;
    }

    private void modifyProperty(Entity entity, String property, Object value, boolean unindexed) {
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            Number number = (Number) value;
            setProperty(entity, property, number.longValue(), unindexed);
        } else if (value instanceof Float) {
            Number number = (Number) value;
            setProperty(entity, property, number.doubleValue(), unindexed);
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                entity.setProperty(property, null);
            } else {
                if (collection instanceof Set) {
                    replaceCollection(entity, property, unindexed, collection, new HashSet());
                } else if (collection instanceof List) {
                    replaceCollection(entity, property, unindexed, collection, new ArrayList(collection.size()));
                }
            }
        } else if (unindexed) {
            entity.setUnindexedProperty(property, value);
        }
    }

    private void setProperty(Entity clone, String name, Object convertedValue, boolean unindexed) {
        if (unindexed) {
            clone.setUnindexedProperty(name, convertedValue);
        } else {
            clone.setProperty(name, convertedValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceCollection(Entity entity, String propertyName, boolean unindexed, Collection collection, Collection convertedCollection) {
        for (Object o : collection) {
            convertedCollection.add(convert(o));
        }
        setProperty(entity, propertyName, convertedCollection, unindexed);
    }

    private Object convert(Object o) {
        if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
            Number number = (Number) o;
            return number.longValue();
        } else if (o instanceof Float) {
            return Number.class.cast(o).doubleValue();
        } else {
            return o;
        }
    }

}
