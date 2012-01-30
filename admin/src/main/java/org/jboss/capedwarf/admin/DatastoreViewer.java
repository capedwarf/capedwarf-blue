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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.html.HtmlPanelGroup;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */
@Named("datastoreViewer")
@RequestScoped
public class DatastoreViewer {

    private String entityKind;

    private static List<String> properties = new ArrayList<String>();
    private static List<Row> rows = new ArrayList<Row>();

    private DynamicTable table = new DynamicTable("#{datastoreViewer.rows}");


    public String getEntityKind() {
        return entityKind;
    }

    public void setEntityKind(String entityKind) {
        this.entityKind = entityKind;
    }

    public List<String> getEntityKinds() {
        Set<String> set = new TreeSet<String>();
        for (Entity entity : getDatastore().prepare(new Query()).asIterable()) {
            if (entity != null) {
                set.add(entity.getKind());
            }
        }
        return new ArrayList<String>(set);
    }

    private DatastoreService getDatastore() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    public String update() {
        loadEntities();
        populateDataTable();
        return "ok";
    }

    public List<Row> getRows() {
        return rows;
    }

    private void populateDataTable() {
        table.clearColumns();
        table.addLinkColumn("key", "Key", "datastoreEntity.cdi?key=#{row.key}");
        table.addColumn("writeOps", "Write ops");
        table.addColumn("idName", "ID/Name");
        for (int i = 0, propertiesSize = properties.size(); i < propertiesSize; i++) {
            String columnHeading = properties.get(i);
            table.addColumn("cells[" + i + "]", columnHeading);
        }
    }

    private void loadEntities() {
        rows.clear();
        SortedSet<String> propertyNameSet = new TreeSet<String>();
        for (Entity entity : getDatastore().prepare(new Query(getEntityKind())).asIterable()) {
            propertyNameSet.addAll(entity.getProperties().keySet());
            rows.add(new Row(entity));
        }

        properties.clear();
        properties.addAll(propertyNameSet);
    }

    public HtmlPanelGroup getDynamicDataTableGroup() {
        populateDataTable();
        return table;
    }

    public void setDynamicDataTableGroup(HtmlPanelGroup dynamicDataTableGroup) {
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
                cells[i] = entity.getProperty(property);
            }
            return cells;
        }
    }
}
