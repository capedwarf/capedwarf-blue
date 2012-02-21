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

import com.google.appengine.api.datastore.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author Marko Luksa
 */
@Named("datastoreViewer")
@RequestScoped
public class DatastoreViewer {

    @Inject @HttpParam
    private String selectedEntityKind;

    private static List<String> properties = new ArrayList<String>();
    private static List<Row> rows = new ArrayList<Row>();

    public String getSelectedEntityKind() {
        return selectedEntityKind;
    }

    public void setSelectedEntityKind(String selectedEntityKind) {
        this.selectedEntityKind = selectedEntityKind;
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

    public List<Row> getRows() {
        return rows;
    }

    public static List<String> getProperties() {
        return properties;
    }

    @PostConstruct
    private void loadEntities() {
        rows.clear();
        SortedSet<String> propertyNameSet = new TreeSet<String>();
        for (Entity entity : getDatastore().prepare(new Query(getSelectedEntityKind())).asIterable()) {
            propertyNameSet.addAll(entity.getProperties().keySet());
            rows.add(new Row(entity));
        }

        properties.clear();
        properties.addAll(propertyNameSet);
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
