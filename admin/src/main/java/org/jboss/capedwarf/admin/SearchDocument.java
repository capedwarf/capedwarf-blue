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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named("searchDocument")
@RequestScoped
public class SearchDocument {

    @Inject
    @HttpParam
    private String namespace;

    @Inject
    @HttpParam
    private String indexName;

    @Inject
    @HttpParam
    private String docId;

    private Document document;

    public String getNamespace() {
        return namespace;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getDocId() {
        return docId;
    }

    public int getOrderId() {
        return getDocument().getRank();
    }

    public List<Property> getProperties() {
        Document document = getDocument();

        List<Property> list = new ArrayList<Property>();
        for (Field field : document.getFields()) {
            list.add(new Property(field));
        }
        return list;
    }

    private Document getDocument() {
        if (document == null) {
            SearchService search = SearchServiceFactory.getSearchService(namespace);
            Index index = search.getIndex(IndexSpec.newBuilder().setName(indexName));
            document = index.get(docId);
        }
        return document;
    }

    public static class Property {

        private Field field;

        public Property(Field field) {
            this.field = field;
        }

        public String getName() {
            return field.getName();
        }

        public String getType() {
            return field.getType().name();
        }

        public String getValue() {
            return SearchUtils.getStringValue(field);
        }
    }

}
