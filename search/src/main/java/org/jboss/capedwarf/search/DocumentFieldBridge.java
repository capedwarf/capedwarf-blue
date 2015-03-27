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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.appengine.api.search.Field;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.spatial.SpatialFieldBridge;
import org.hibernate.search.spatial.SpatialFieldBridgeByHash;
import org.hibernate.search.spatial.impl.Point;

import static org.apache.lucene.document.Field.Index;
import static org.apache.lucene.document.Field.Store;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DocumentFieldBridge implements FieldBridge {

    private FieldNamePrefixer fieldNamePrefixer = new FieldNamePrefixer();

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private SpatialFieldBridge spatialFieldBridgeByGrid = new SpatialFieldBridgeByHash();

    @SuppressWarnings("unchecked")
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        com.google.appengine.api.search.Document googleDocument = (com.google.appengine.api.search.Document) value;
        document.add(new org.apache.lucene.document.Field(CacheValue.MATCH_ALL_DOCS_FIELD_NAME, CacheValue.MATCH_ALL_DOCS_FIELD_VALUE, Store.NO, Index.NOT_ANALYZED_NO_NORMS));
        for (Field field : googleDocument.getFields()) {
            if (field.getType() == null) {
                throw new IllegalStateException("Field " + field.getName() + " of document " + googleDocument.getId() + " has null type!");
            }
            String prefixedFieldName = fieldNamePrefixer.getPrefixedFieldName(field.getName(), field.getType());
            String prefixedAllFieldName = fieldNamePrefixer.getPrefixedFieldName(CacheValue.ALL_FIELD_NAME, field.getType());
            if (field.getType() == Field.FieldType.NUMBER) {
                luceneOptions.addNumericFieldToDocument(prefixedFieldName, field.getNumber(), document);
                luceneOptions.addNumericFieldToDocument(prefixedAllFieldName, field.getNumber(), document);
            } else if (field.getType() == Field.FieldType.GEO_POINT) {
                spatialFieldBridgeByGrid.set(
                    prefixedFieldName,
                    Point.fromDegrees(field.getGeoPoint().getLatitude(), field.getGeoPoint().getLongitude()),
                    document, luceneOptions
                );

                document.getFields();
            } else {
                luceneOptions.addFieldToDocument(prefixedFieldName, convertToString(field), document);
                luceneOptions.addFieldToDocument(prefixedAllFieldName, convertToString(field), document);
            }
        }
    }

    public static String convertToString(Field field) {
        switch (field.getType()) {
            case ATOM:
                return field.getAtom();
            case TEXT:
                return field.getText();
            case HTML:
                return field.getHTML();
            case DATE:
                return DATE_FORMAT.format(field.getDate());
            default:
                throw new IllegalArgumentException("Unexpected field type " + field.getType() + " (field '" + field.getName() + "')");
        }
    }
}
