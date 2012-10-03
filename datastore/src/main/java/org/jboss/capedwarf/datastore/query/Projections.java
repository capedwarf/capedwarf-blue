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

package org.jboss.capedwarf.datastore.query;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.ProjectionConstants;

/**
 * Handle query projections.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class Projections {
    private static final String TYPES_FIELD = "__capedwarf___TYPES___";
    private static final int OFFSET = 2;

    private Properties bridges = new Properties();

    Projections() {
    }

    /**
     * Apply GAE projections onto Cache projections.
     *
     * @param gaeQuery the GAE query
     * @param cacheQuery the cache query
     */
    static void applyProjections(Query gaeQuery, CacheQuery cacheQuery) {
        if (gaeQuery.isKeysOnly()) {
            cacheQuery.projection(ProjectionConstants.KEY);
        } else if (gaeQuery.getProjections().size() > 0) {
            List<String> projections = new ArrayList<String>(OFFSET + gaeQuery.getProjections().size());
            projections.add(ProjectionConstants.KEY);
            projections.add(TYPES_FIELD);
            for (Projection projection : gaeQuery.getProjections()) {
                if (projection instanceof PropertyProjection) {
                    PropertyProjection propertyProjection = (PropertyProjection) projection;
                    projections.add(propertyProjection.getName());
                } else {
                    throw new IllegalStateException("Unsupported projection type: " + projection.getClass());
                }
            }
            cacheQuery.projection(projections.toArray(new String[projections.size()]));
        }
    }

    /**
     * Store property's bridge.
     *
     * @param propertyName the property name
     * @param bridge the bridge
     */
    void storePropertyBridge(String propertyName, Bridge bridge) {
        bridges.put(propertyName, String.valueOf(bridge.ordinal()));
    }

    /**
     * Store bridges to document.
     *
     * @param document the Lucene document
     */
    void finish(Document document) {
        try {
            StringWriter writer = new StringWriter();
            bridges.store(writer, null);
            document.add(new Field(TYPES_FIELD, writer.toString(), Field.Store.YES, Field.Index.NO));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot store bridges!", e);
        }
    }

    /**
     * Read bridges.
     *
     * @param field the types field
     * @return bridges
     */
    static Properties readPropertiesBridges(String field) {
        try {
            Properties bridges = new Properties();
            bridges.load(new StringReader(field));
            return bridges;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read bridges!", e);
        }
    }

    /**
     * Convert to entity.
     *
     * @param query the GAE query
     * @param result the current result
     * @return Entity instance
     */
    static Entity convertToEntity(Query query, Object result) {
        if (result instanceof Entity) {
            return Entity.class.cast(result);
        } else {
            final Object[] row = (Object[]) result;
            final Entity entity = new Entity((Key) row[0]);
            if (row.length > 1) {
                final Properties bridges = readPropertiesBridges(row[1].toString());
                int i = OFFSET;
                for (Projection projection : query.getProjections()) {
                    if (projection instanceof PropertyProjection) {
                        final PropertyProjection propertyProjection = (PropertyProjection) projection;
                        final String propertyName = propertyProjection.getName();
                        Object value = row[i];
                        if (value instanceof String) {
                            final String bridgeOrdinal = bridges.getProperty(propertyName);
                            final Bridge bridge = Bridge.values()[Integer.parseInt(bridgeOrdinal)];
                            value = bridge.stringToObject(value.toString());
                        }
                        entity.setProperty(propertyName, value);
                    } else {
                        throw new IllegalStateException("Unsupported projection type: " + projection.getClass());
                    }
                    i++;
                }
            }
            return entity;
        }
    }
}
