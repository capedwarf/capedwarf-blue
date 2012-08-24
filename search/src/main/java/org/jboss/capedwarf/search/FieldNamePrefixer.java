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

import com.google.appengine.api.search.Field;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class FieldNamePrefixer {

    public static final String DELIMITER = "___";

    public Context.Field getPrefixedField(Context.Field field, Field.FieldType fieldType) {
        if (field instanceof Context.SimpleField) {
            Context.SimpleField simpleField = (Context.SimpleField) field;
            return new Context.SimpleField(getPrefixedFieldName(simpleField.getName(), fieldType));
        } else if (field instanceof Context.DistanceFunction) {
            Context.DistanceFunction distanceFunction = (Context.DistanceFunction) field;
            return new Context.DistanceFunction(getPrefixedFieldName(distanceFunction.getFieldName(), fieldType), distanceFunction.getGeoPoint());
        } else {
            return field; // TODO
        }
    }

    public String getPrefixedFieldName(String name, Field.FieldType fieldType) {
        return fieldType.name() + DELIMITER + name;
    }

    public Field.FieldType getFieldType(String prefixedFieldName) {
        int delimiterIndex = prefixedFieldName.indexOf(DELIMITER);
        if (delimiterIndex == -1) {
            throw new IllegalArgumentException("Field name is not prefixed: " + prefixedFieldName);
        }
        String prefix = prefixedFieldName.substring(0, delimiterIndex);
        return Field.FieldType.valueOf(prefix);
    }
}
