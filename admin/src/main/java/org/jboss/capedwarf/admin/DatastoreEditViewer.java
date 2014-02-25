/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

/**
 * @author Ales Justin
 */
@Named("datastoreEdit")
@RequestScoped
public class DatastoreEditViewer extends DatastoreEntityHolder {
    public List<Property> getProperties() {
        try {
            Entity entity = getEntity();
            List<Property> results = new ArrayList<>();
            for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
                Object value = entry.getValue();
                if (hasPropertyEditor(value)) {
                    results.add(new Property(entry.getKey(), value, value != null ? value.getClass().getName() : "NULL"));
                }
            }
            return results;
        } catch (EntityNotFoundException e) {
            return Collections.emptyList();
        }
    }

    public static class Property {
        private String key;
        private Object value;
        private String type;

        public Property(String key, Object value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }
}
