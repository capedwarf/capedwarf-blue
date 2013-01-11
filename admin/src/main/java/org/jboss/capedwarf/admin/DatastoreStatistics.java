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

package org.jboss.capedwarf.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.jboss.capedwarf.datastore.NamespaceServiceFactory;
import org.jboss.capedwarf.datastore.NamespaceServiceInternal;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static org.jboss.capedwarf.admin.NumberFormatter.formatBytes;
import static org.jboss.capedwarf.admin.NumberFormatter.formatCount;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named("datastoreStatistics")
@RequestScoped
@SuppressWarnings("UnusedDeclaration")
public class DatastoreStatistics extends DatastoreHolder {

    public static final String ALL_NAMESPACES = "!";

    @Inject @HttpParam
    private String selectedNamespace = ALL_NAMESPACES;

    @Inject @HttpParam
    private String selectedEntityKind;

    private Entity statEntity;

    @PostConstruct
    private void loadStatEntity() {
        if (getSelectedNamespace().equals(ALL_NAMESPACES)) {
            NamespaceManager.set("");
            if (selectedEntityKind == null || selectedEntityKind.equals("")) {
                statEntity = getStatEntity("__Stat_Total__");
            } else {
                statEntity = getStatEntity("__Stat_Kind__", selectedEntityKind);
            }
        } else {
            NamespaceManager.set(getSelectedNamespace());
            try {
                if (selectedEntityKind == null || selectedEntityKind.equals("")) {
                    statEntity = getStatEntity("__Stat_Ns_Total__");
                } else {
                    statEntity = getStatEntity("__Stat_Ns_Kind__", selectedEntityKind);
                }
            } finally {
                NamespaceManager.set("");
            }
        }
    }

    private Entity getStatEntity(String statEntityKind, String entityKind) {
        Query query = new Query(statEntityKind)
            .setFilter(new Query.FilterPredicate("kind_name", EQUAL, entityKind))
            .addSort("timestamp", Query.SortDirection.DESCENDING)
            ;
        List<Entity> list = getDatastore().prepare(query).asList(FetchOptions.Builder.withLimit(1));
        return list.isEmpty() ? null : list.get(0);
    }

    private Entity getStatEntity(String kind) {
        Query query = new Query(kind)
            .addSort("timestamp", Query.SortDirection.DESCENDING);
        List<Entity> list = getDatastore().prepare(query).asList(FetchOptions.Builder.withLimit(1));
        return list.isEmpty() ? null : list.get(0);
    }

    public Column getEntities() {
        if (statEntity == null) {
            loadStatEntity();
        }

        if (statEntity == null) {
            return new Column(0, 0);
        } else {
            return new Column((Long) statEntity.getProperty("bytes"), (Long) statEntity.getProperty("count"));
        }
    }

    public Column getBuiltInIndexes() {
        return new Column(0, 0);
    }

    public Column getCompositeIndexes() {
        return new Column(0, 0);
    }

    public Column getTotal() {
        Column column = new Column(0, 0);
        column.add(getEntities());
        column.add(getBuiltInIndexes());
        column.add(getCompositeIndexes());
        return column;
    }

    public String getSelectedEntityKind() {
        return selectedEntityKind;
    }

    public void setSelectedEntityKind(String selectedEntityKind) {
        this.selectedEntityKind = selectedEntityKind;
    }

    public String getSelectedNamespace() {
        return selectedNamespace == null ? ALL_NAMESPACES : selectedNamespace;
    }

    public void setSelectedNamespace(String selectedNamespace) {
        this.selectedNamespace = selectedNamespace;
    }

    public Set<String> getNamespaces() {
        NamespaceServiceInternal namespaceService = NamespaceServiceFactory.getNamespaceService();
        return namespaceService.getNamespaces();
    }

    public Set<String> getEntityKinds() {
        NamespaceServiceInternal namespaceService = NamespaceServiceFactory.getNamespaceService();
        if (getSelectedNamespace().equals(ALL_NAMESPACES)) {
            Set<String> allKinds = new HashSet<String>();
            for (String ns : namespaceService.getNamespaces()) {
                allKinds.addAll(namespaceService.getKindsPerNamespace(ns));
            }
            return allKinds;
        } else {
            return namespaceService.getKindsPerNamespace(getSelectedNamespace());
        }
    }

    public static class Column {
        private long bytes;
        private long count;

        public Column(long bytes, long count) {
            this.bytes = bytes;
            this.count = count;
        }

        public String getTotalSize() {
            return formatBytes(bytes);
        }

        public String getEntryCount() {
            return formatCount(count);
        }

        public String getAverageSize() {
            return count == 0 ? "" : formatBytes(bytes / count);
        }

        public void add(Column column) {
            bytes += column.bytes;
            count += column.count;
        }
    }

}
