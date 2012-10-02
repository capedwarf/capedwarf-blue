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
    private static final String TYPES_FIELD = "__TYPES";
    private static final int OFFSET = 2;

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
     * Store types.
     *
     * @param document the Lucene document
     * @param types the properties types
     */
    static void storePropertiesTypes(Document document, Properties types) {
        try {
            StringWriter writer = new StringWriter();
            types.store(writer, "PropertyMap types.");
            document.add(new Field(TYPES_FIELD, writer.toString(), Field.Store.YES, Field.Index.NO));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot store types!", e);
        }
    }

    /**
     * Read types.
     *
     * @param field the types field
     * @return types
     */
    static Properties readPropertiesTypes(String field) {
        try {
            Properties types = new Properties();
            types.load(new StringReader(field));
            return types;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read types!", e);
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
            Object[] row = (Object[]) result;
            Entity entity = new Entity((Key) row[0]);
            if (row.length > 1) {
                Properties types = readPropertiesTypes(row[1].toString());
                int i = OFFSET;
                for (Projection projection : query.getProjections()) {
                    if (projection instanceof PropertyProjection) {
                        PropertyProjection propertyProjection = (PropertyProjection) projection;
                        String propertyName = propertyProjection.getName();
                        String type = types.getProperty(propertyName);
                        entity.setProperty(propertyName, PropertyMapBridge.covertToValue(type, row[i]));
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
