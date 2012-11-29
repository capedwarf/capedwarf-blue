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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named("search")
@RequestScoped
public class Search {

    @Inject
    @HttpParam
    private String namespace;

    @Inject
    @HttpParam
    private String indexNamePrefix;

    private List<Row> rows;


    public String getNamespace() {
        return namespace == null ? "" : namespace;
    }

    public String getIndexNamePrefix() {
        return indexNamePrefix == null ? "" : indexNamePrefix;
    }

    public boolean isSearchPerformed() {
        return namespace != null && indexNamePrefix != null;
    }


    public List<Row> getRows() {
        if (rows == null) {
            loadRows();
        }
        return rows;
    }

    @PostConstruct
    private void loadRows() {
        rows = new ArrayList<Row>();
        NamespaceManager.set(namespace);
        try {
            SearchService search = SearchServiceFactory.getSearchService(namespace);
            GetResponse<Index> response = search.getIndexes(GetIndexesRequest.newBuilder().setIndexNamePrefix(indexNamePrefix).build());
            for (Index index : response.getResults()) {
                rows.add(new Row(index.getName(), index.getConsistency()));
            }
        } finally {
            NamespaceManager.set("");
        }
    }

    public class Row {
        private String indexName;
        private Consistency consistency;

        public Row(String name, Consistency consistency) {
            this.indexName = name;
            this.consistency = consistency;
        }

        public String getIndexName() {
            return indexName;
        }

        public String getConsistency() {
            return consistency.name();
        }

    }

}
