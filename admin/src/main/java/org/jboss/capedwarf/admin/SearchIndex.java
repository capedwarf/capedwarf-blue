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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named("searchIndex")
@RequestScoped
public class SearchIndex {

    @Inject
    @HttpParam
    private String namespace;

    @Inject
    @HttpParam
    private String indexName;

    private List<String> fieldNames = new ArrayList<String>();
    private List<Row> rows;


    public String getNamespace() {
        return namespace;
    }

    public String getIndexName() {
        return indexName;
    }

    public List<Row> getRows() {
        if (rows == null) {
            loadEntities();
        }
        return rows;
    }

    public List<String> getFieldNames() {
        if (rows == null) {
            loadEntities();
        }
        return fieldNames;
    }

    @PostConstruct
    private void loadEntities() {
        rows = new ArrayList<Row>();
        SortedSet<String> fieldNameSet = new TreeSet<String>();
        NamespaceManager.set(namespace);
        try {
            SearchService search = SearchServiceFactory.getSearchService(namespace);
            Index index = search.getIndex(IndexSpec.newBuilder().setName(indexName));
            GetResponse<Document> response = index.getRange(GetRequest.newBuilder().build());
            for (Document document : response.getResults()) {
                fieldNameSet.addAll(document.getFieldNames());
                rows.add(new Row(document));
            }
        } finally {
            NamespaceManager.set("");
        }
        fieldNames.clear();
        fieldNames.addAll(fieldNameSet);
    }

    public class Row {
        private Document document;

        public Row(Document document) {
            this.document = document;
        }

        public String getDocId() {
            return document.getId();
        }

        public int getOrderId() {
            return document.getRank();
        }

        public Object[] getCells() {
            Object[] cells = new Object[fieldNames.size()];
            for (int i = 0; i < cells.length; i++) {
                String fieldName = fieldNames.get(i);
                Field field = getOnlyField(fieldName);
                if (field == null) {
                    cells[i] = "";
                } else {
                    cells[i] = SearchUtils.getStringValue(field);
                }
            }
            return cells;
        }

        private Field getOnlyField(String fieldName) {
            try {
                return document.getOnlyField(fieldName);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

    }

}
