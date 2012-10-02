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

package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class EntityKeyBridge implements FieldBridge {

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Key key = (Key) value;
        addKeyField(key, document, luceneOptions);
        addAncestorFields(key, document, luceneOptions);
    }

    private void addKeyField(Key key, Document document, LuceneOptions luceneOptions) {
        addField(document, luceneOptions, key, Entity.KEY_RESERVED_PROPERTY);
    }

    private void addAncestorFields(Key key, Document document, LuceneOptions luceneOptions) {
        while (key != null) {
            addAncestorField(key, document, luceneOptions);
            key = key.getParent();
        }
    }

    private void addAncestorField(Key key, Document document, LuceneOptions luceneOptions) {
        addField(document, luceneOptions, key, QueryConverter.ANCESTOR_PROPERTY_KEY);
    }

    private void addField(Document document, LuceneOptions luceneOptions, Key key, String fieldName) {
        luceneOptions.addFieldToDocument(fieldName, Bridge.KEY.objectToString(key), document);
    }

}

