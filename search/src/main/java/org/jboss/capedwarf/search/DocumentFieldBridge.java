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
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.DateBridge;
import org.hibernate.search.bridge.impl.BridgeFactory;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DocumentFieldBridge implements FieldBridge {

    @SuppressWarnings("unchecked")
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        com.google.appengine.api.search.Document googleDocument = (com.google.appengine.api.search.Document) value;
        for (Field field : googleDocument.getFields()) {
            luceneOptions.addFieldToDocument(field.getName(), convertToString(field), document);
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
                return DateBridge.DATE_DAY.objectToString(field.getDate());
            case NUMBER:
                return BridgeFactory.DOUBLE.objectToString(field.getNumber());
            default:
                throw new IllegalArgumentException("Unexpected field type " + field.getType() + " (field '" + field.getName() + "')");
        }
    }
}
