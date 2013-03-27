/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.jboss.capedwarf.datastore.PropertyUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PropertyMapBridge implements FieldBridge {
    @SuppressWarnings("unchecked")
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        final Projections projections = new Projections();
        final Map<String, ?> entityProperties = (Map<String, ?>) value;
        for (Map.Entry<String, ?> entry : entityProperties.entrySet()) {
            final String propertyName = entry.getKey();
            final Object propertyValue = entry.getValue();
            if (PropertyUtils.isIndexedProperty(propertyValue)) {
                final Bridge bridge = BridgeUtils.matchBridge(propertyValue);
                if (propertyValue instanceof Collection) {
                    Collection collection = (Collection) propertyValue;
                    for (Object element : collection) {
                        if (PropertyUtils.isIndexedProperty(element)) {
                            final Bridge inner = BridgeUtils.matchBridge(element);
                            luceneOptions.addFieldToDocument(propertyName, inner.objectToString(element), document);
                        }
                    }
                } else {
                    luceneOptions.addFieldToDocument(propertyName, bridge.objectToString(propertyValue), document);
                }
                projections.storePropertyBridge(propertyName, bridge);
            }
        }
        projections.finish(document);
    }
}

