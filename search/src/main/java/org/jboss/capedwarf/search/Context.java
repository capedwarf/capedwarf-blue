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

package org.jboss.capedwarf.search;

import com.google.appengine.api.search.GeoPoint;
import org.apache.lucene.search.Query;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class Context {

    private Query query;
    private Field field;
    private Operator operator;
    private boolean onGlobalField;

    public Context() {
    }

    public Context(Query query, Field field, Operator operator) {
        this.query = query;
        this.field = field;
        this.operator = operator;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Query getQuery() {
        return query;
    }

    protected void setQuery(Query query) {
        this.query = query;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOnGlobalField(boolean onGlobalField) {
        this.onGlobalField = onGlobalField;
    }

    public boolean isOnGlobalField() {
        return onGlobalField;
    }

    public static class Field {
    }

    public static class SimpleField extends Field {
        private String name;

        public SimpleField(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Function extends Field {
    }

    public static class DistanceFunction extends Function {

        private String fieldName;
        private GeoPoint geoPoint;

        public DistanceFunction() {
        }

        public DistanceFunction(String fieldName, GeoPoint geoPoint) {
            this.fieldName = fieldName;
            this.geoPoint = geoPoint;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public GeoPoint getGeoPoint() {
            return geoPoint;
        }

        public void setGeoPoint(GeoPoint geoPoint) {
            this.geoPoint = geoPoint;
        }
    }
}
