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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.RawValue;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.SearchException;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.ProjectionConstants;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.shared.config.IndexesXml;

/**
 * Handle query projections.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
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
     * @param gaeQuery   the GAE query
     * @param cacheQuery the cache query
     */
    static void applyProjections(Query gaeQuery, CacheQuery cacheQuery) {
        List<String> projections = getProjections(gaeQuery);
        if (projections.isEmpty() == false) {
            cacheQuery.projection(projections.toArray(new String[projections.size()]));
            if (!gaeQuery.isKeysOnly()) {
                String fullTextFilterName = getFullTextFilterName(gaeQuery);
                try {
                    cacheQuery.enableFullTextFilter(fullTextFilterName);
                } catch (SearchException e) {
                    throw new DatastoreNeedIndexException("No matching index found (FullTextFilterName: " + fullTextFilterName + ")");
                }
            }
        }
    }

    private static String getFullTextFilterName(Query gaeQuery) {
        for (IndexesXml.Index index : CapedwarfEnvironment.getThreadLocalInstance().getIndexes().getIndexes().values()) {
            if (indexMatches(index, gaeQuery)) {
                return index.getName();
            }
        }
        throw new DatastoreNeedIndexException("No matching index found");
    }

    private static boolean indexMatches(IndexesXml.Index index, Query query) {
        if (!index.getKind().equals(query.getKind())) {
            return false;
        }

        Set<String> filterProperties = getFilterProperties(query);
        Set<String> sortProperties = getSortProperties(query);
        Set<String> projectionProperties = getProjectionProperties(query);
        sortProperties.removeAll(filterProperties);
        projectionProperties.removeAll(filterProperties);
        projectionProperties.removeAll(sortProperties);

        List<String> indexProperties = index.getPropertyNames();

        while (!indexProperties.isEmpty()) {
            String property = indexProperties.get(0);
            if (!filterProperties.isEmpty()) {
                if (filterProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else if (!sortProperties.isEmpty()) {
                if (sortProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else if (!projectionProperties.isEmpty()) {
                if (projectionProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return indexProperties.isEmpty() && filterProperties.isEmpty() && sortProperties.isEmpty() && projectionProperties.isEmpty();
    }

    @SuppressWarnings("deprecation")
    private static Set<String> getFilterProperties(Query query) {
        Set<String> set = new HashSet<>();
        addFilterPropertiesToSet(set, query.getFilter());
        for (Query.FilterPredicate predicate : query.getFilterPredicates()) {
            addFilterPropertiesToSet(set, predicate);
        }
        return set;
    }

    private static void addFilterPropertiesToSet(Set<String> set, Query.Filter filter) {
        if (filter == null) {
            return;
        }
        if (filter instanceof Query.FilterPredicate) {
            Query.FilterPredicate predicate = (Query.FilterPredicate) filter;
            set.add(predicate.getPropertyName());
        } else if (filter instanceof Query.CompositeFilter) {
            Query.CompositeFilter composite = (Query.CompositeFilter) filter;
            for (Query.Filter subFilter : composite.getSubFilters()) {
                addFilterPropertiesToSet(set, subFilter);
            }
        } else {
            throw new IllegalArgumentException("Unsupported filter type " + filter);
        }
    }

    private static Set<String> getSortProperties(Query query) {
        Set<String> set = new HashSet<>();
        for (Query.SortPredicate sortPredicate : query.getSortPredicates()) {
            set.add(sortPredicate.getPropertyName());
        }
        return set;
    }

    private static Set<String> getProjectionProperties(Query query) {
        Set<String> set = new HashSet<>();
        for (Projection projection : query.getProjections()) {
            set.add(getPropertyName(projection));
        }
        return set;
    }

    private static List<String> getProjections(Query gaeQuery) {
        List<String> projections = new ArrayList<String>();
        if (gaeQuery.isKeysOnly()) {
            projections.add(ProjectionConstants.KEY);
            projections.add(TYPES_FIELD);
            projections.addAll(getPropertiesRequiredOnlyForSorting(gaeQuery));
        } else if (gaeQuery.getProjections().size() > 0) {
            projections.add(ProjectionConstants.KEY);
            projections.add(TYPES_FIELD);
            for (Projection projection : gaeQuery.getProjections()) {
                projections.add(getPropertyName(projection));
            }
            projections.addAll(getPropertiesRequiredOnlyForSorting(gaeQuery));
        }

        return projections;
    }

    public static List<String> getPropertiesRequiredOnlyForSorting(Query gaeQuery) {
        List<String> list = new ArrayList<String>();
        QueryResultProcessor processor = new QueryResultProcessor(gaeQuery);
        if (processor.isProcessingNeeded()) {
            for (String propertyName : processor.getPropertiesUsedInIn()) {
                if (isOnlyNeededForSorting(propertyName, gaeQuery)) {
                    list.add(propertyName);
                }
            }
        }
        return list;
    }

    private static String getPropertyName(Projection projection) {
        if (projection instanceof PropertyProjection) {
            PropertyProjection propertyProjection = (PropertyProjection) projection;
            return propertyProjection.getName();
        } else {
            throw new IllegalStateException("Unsupported projection type: " + projection.getClass());
        }
    }

    /**
     * Store property's bridge.
     *
     * @param propertyName the property name
     * @param bridge       the bridge
     */
    void storePropertyBridge(String propertyName, Bridge bridge) {
        bridges.put(propertyName, String.valueOf(bridge.name()));
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
     * @param query  the GAE query
     * @param result the current result
     * @return Entity instance
     */
    static Entity convertToEntity(Query query, Object result) {
        if (result instanceof Entity) {
            return Entity.class.cast(result);
        }

        final Object[] row = (Object[]) result;
        final Entity entity = new Entity((Key) row[0]);
        if (row.length > 1) {
            final Properties bridges = readPropertiesBridges(row[1].toString());
            int i = OFFSET;
            for (Projection projection : query.getProjections()) {
                if (projection instanceof PropertyProjection) {
                    PropertyProjection pp = (PropertyProjection) projection;
                    String propertyName = pp.getName();
                    Object value;
                    Bridge bridge = getBridge(propertyName, bridges);
                    if (mustBeWrappedInRawValue(pp)) {
                        value = bridge.getValue((String) row[i]);
                        value = newRawValue(value);
                    } else {
                        Class<?> type = pp.getType();
                        if (type != null && bridge.isAssignableTo(type) == false) {
                            throw new IllegalArgumentException("Wrong projection type: " + pp);
                        }
                        value = convert(bridge, row[i]);
                    }
                    entity.setProperty(propertyName, value);
                } else {
                    throw new IllegalStateException("Unsupported projection type: " + projection.getClass());
                }
                i++;
            }
            for (String propertyName : getPropertiesRequiredOnlyForSorting(query)) {
                Object value = convert(propertyName, row[i], bridges);
                entity.setProperty(propertyName, value);
                i++;
            }
        }
        return entity;
    }

    private static boolean mustBeWrappedInRawValue(PropertyProjection propertyProjection) {
        return propertyProjection.getType() == null;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean isOnlyNeededForSorting(String propertyName, Query query) {
        if (query.isKeysOnly()) {
            return true;
        } else if (query.getProjections().size() > 0) {
            return isProjectedProperty(propertyName, query) == false;
        } else {
            return false;
        }
    }

    private static boolean isProjectedProperty(String propertyName, Query query) {
        for (Projection projection : query.getProjections()) {
            if (projection instanceof PropertyProjection) {
                PropertyProjection propertyProjection = (PropertyProjection) projection;
                if (propertyProjection.getName().equals(propertyName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Object convert(Bridge bridge, Object o) {
        if (o instanceof String) {
            return bridge.stringToObject(o.toString());
        }
        return o;
    }

    private static Object convert(String propertyName, Object o, Properties bridges) {
        if (o instanceof String) {
            final Bridge bridge = getBridge(propertyName, bridges);
            return bridge.stringToObject(o.toString());
        }
        return o;
    }

    private static RawValue newRawValue(Object value) {
        return ReflectionUtils.newInstance(RawValue.class, new Class[]{Object.class}, new Object[]{value});
    }

    private static Bridge getBridge(String propertyName, Properties bridges) {
        String bridgeName = bridges.getProperty(propertyName);
        return Bridge.valueOf(bridgeName);
    }
}
