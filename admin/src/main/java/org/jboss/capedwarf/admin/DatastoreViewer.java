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

package org.jboss.capedwarf.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.capedwarf.datastore.NamespaceServiceFactory;
import org.jboss.capedwarf.datastore.NamespaceServiceInternal;
import org.jboss.capedwarf.gql4j.GqlQuery;

/**
 * @author Marko Luksa
 * @author Ales Justin
 */
@SuppressWarnings("CdiInjectionPointsInspection")
@Named("datastoreViewer")
@RequestScoped
public class DatastoreViewer extends DatastoreHolder {

    private static final int DEFAULT_ROWS_PER_PAGE = 20;

    @Inject @HttpParam
    private String selectedNamespace = "";

    @Inject @HttpParam
    private String selectedEntityKind;

    @Inject @HttpParam
    private String query;

    @Inject @HttpParam
    private String key;

    @Inject @HttpParam
    private String page;

    private List<String> properties = new ArrayList<String>();
    private List<Row> rows;

    private int resultCount;

    public String getSelectedEntityKind() {
        return selectedEntityKind;
    }

    public String getSelectedNamespace() {
        return VelocityUtils.toString(selectedNamespace);
    }

    public void setSelectedEntityKind(String selectedEntityKind) {
        this.selectedEntityKind = selectedEntityKind;
    }

    public Key getKey() {
        return (key != null) ? KeyFactory.stringToKey(key) : null;
    }

    public Set<String> getNamespaces() {
        NamespaceServiceInternal namespaceService = NamespaceServiceFactory.getNamespaceService();
        return namespaceService.getNamespaces();
    }

    public List<String> getEntityKinds() {
        NamespaceServiceInternal namespaceService = NamespaceServiceFactory.getNamespaceService();
        Set<String> set = namespaceService.getKindsPerNamespace(getSelectedNamespace());
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    public List<Row> getRows() {
        if (rows == null) {
            loadEntities();
        }
        return rows;
    }

    public List<String> getProperties() {
        if (rows == null) {
            loadEntities();
        }
        return properties;
    }

    @PostConstruct
    private void loadEntities() {
        if (key != null) {
            getDatastore().delete(KeyFactory.stringToKey(key));
        }

        rows = new ArrayList<Row>();
        SortedSet<String> propertyNameSet = new TreeSet<String>();

        final String previous = NamespaceManager.get();
        NamespaceManager.set(selectedNamespace);
        try {
            Iterable<Entity> entities = findEntities();
            for (Entity entity : entities) {
                propertyNameSet.addAll(entity.getProperties().keySet());
                rows.add(new Row(entity));
            }
        } finally {
            NamespaceManager.set(previous);
        }

        properties.clear();
        properties.addAll(propertyNameSet);
    }

    private Iterable<Entity> findEntities() {
        Query q;
        FetchOptions options;
        if (query != null && query.length() > 0) {
            GqlQuery gql = new GqlQuery(query);
            q = gql.query();
            options = gql.fetchOptions();
        } else {
            q = new Query(getSelectedEntityKind());
            options = FetchOptions.Builder.withDefaults();
        }
        options = options.offset(getOffset()).limit(getLimit());

        PreparedQuery preparedQuery = getDatastore().prepare(q);
        resultCount = preparedQuery.countEntities(FetchOptions.Builder.withDefaults());
        return preparedQuery.asIterable(options);
    }

    public int getOffset() {
        return (getCurrentPage()-1) * DEFAULT_ROWS_PER_PAGE;
    }

    public int getLimit() {
        return DEFAULT_ROWS_PER_PAGE;
    }

    public int getNumberOfPages() {
        return ((resultCount - 1) / DEFAULT_ROWS_PER_PAGE) + 1;
    }

    public int getCurrentPage() {
        return page == null ? 1 : Integer.parseInt(page);
    }

    public class Row {
        private Entity entity;

        public Row(Entity entity) {
            this.entity = entity;
        }

        public String getKey() {
            return KeyFactory.keyToString(entity.getKey());
        }

        public String getWriteOps() {
            return "N/A";
        }

        public String getIdName() {
            Key key = entity.getKey();
            return key.getId() == -1 ? key.getName() : String.valueOf(key.getId());
        }

        public Object[] getCells() {
            Object[] cells = new Object[properties.size()];
            for (int i = 0; i < cells.length; i++) {
                String property = properties.get(i);
                Object value = entity.getProperty(property);
                cells[i] = value == null ? "" : value;
            }
            return cells;
        }
    }
}
