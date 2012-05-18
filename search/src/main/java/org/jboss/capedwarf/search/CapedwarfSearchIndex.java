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

import com.google.appengine.api.search.AddDocumentsResponse;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.ListRequest;
import com.google.appengine.api.search.ListResponse;
import com.google.appengine.api.search.OperationResult;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.Schema;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.StatusCode;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class CapedwarfSearchIndex implements Index {

    private String name;
    private String namespace;
    private Consistency consistency;

    private Cache<CacheKey, CacheValue> cache;
    private SearchManager searchManager;

    public CapedwarfSearchIndex(String name, String namespace, Consistency consistency, Cache<CacheKey, CacheValue> cache) {
        this.name = name;
        this.namespace = namespace;
        this.consistency = consistency;
        this.cache = cache;
        this.searchManager = Search.getSearchManager(cache);
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public Consistency getConsistency() {
        return consistency;
    }

    public Future<Void> removeAsync(String... documentIds) {
        return removeAsync(Arrays.asList(documentIds));
    }

    public Future<Void> removeAsync(Iterable<String> documentIds) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<AddDocumentsResponse> addAsync(Document... documents) {
        return addAsync(Arrays.asList(documents));
    }

    public Future<AddDocumentsResponse> addAsync(Iterable<Document> documents) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Results<ScoredDocument>> searchAsync(String queryString) {
        return searchAsync(Query.newBuilder().build(queryString));
    }

    public Future<Results<ScoredDocument>> searchAsync(Query query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<ListResponse<Document>> listDocumentsAsync(ListRequest listRequest) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove(String... documentIds) {
        remove(Arrays.asList(documentIds));
    }

    public void remove(Iterable<String> documentIds) {
        for (String documentId : documentIds) {
            cache.remove(getCacheKey(documentId));
        }
    }

    private CacheKey getCacheKey(String documentId) {
        return new CacheKey(getName(), getNamespace(), documentId);
    }

    private CacheValue getCacheValue(Document document) {
        return new CacheValue(getName(), getNamespace(), document);
    }

    public AddDocumentsResponse add(Document... documents) {
        return add(Arrays.asList(documents));
    }

    public AddDocumentsResponse add(Iterable<Document> documents) {
        List<Document> documentList = new ArrayList<Document>();
        List<String> documentIds = new ArrayList<String>();
        for (Document document : documents) {
            assignIdIfNeeded(document);
            cache.put(getCacheKey(document.getId()), getCacheValue(document));
            documentList.add(document);
            documentIds.add(document.getId());
        }

        return ReflectionUtils.newInstance(
            AddDocumentsResponse.class,
            new Class[]{List.class, List.class},
            new Object[]{documentList, documentIds});
    }

    private void assignIdIfNeeded(Document document) {
        if (document.getId() == null) {
            ReflectionUtils.setFieldValue(document, "documentId", generateId(document));
        }
    }

    private String generateId(Document document) {
        return UUID.randomUUID().toString();
    }

    public Results<ScoredDocument> search(String queryString) {
        return search(Query.newBuilder().build(queryString));
    }

    public Results<ScoredDocument> search(Query query) {
        CacheQuery cacheQuery = searchManager.getQuery(
            createQueryBuilder().bool()
                .must(createIndexAndNamespaceQuery())
                .must(createLuceneQuery(query)).createQuery());

        List<ScoredDocument> scoredDocuments = new ArrayList<ScoredDocument>();
        for (Object o : cacheQuery.list()) {
            CacheValue cacheValue = (CacheValue) o;
            scoredDocuments.add(createScoredDocument(cacheValue.getDocument()));
        }

        OperationResult operationResult = new OperationResult(StatusCode.OK, null);
        return newResults(operationResult, scoredDocuments, scoredDocuments.size(), scoredDocuments.size(), null);
    }

    private org.apache.lucene.search.Query createIndexAndNamespaceQuery() {
        QueryBuilder queryBuilder = createQueryBuilder();
        return queryBuilder.bool()
            .must(queryBuilder.keyword().onField("indexName").matching(getName()).createQuery())
            .must(queryBuilder.keyword().onField("namespace").matching(normalizeNamespace(getNamespace())).createQuery())
            .createQuery();
    }

    private String normalizeNamespace(String namespace) {
        return namespace.isEmpty() ? CacheValue.EMPTY_NAMESPACE : namespace;
    }

    private QueryBuilder createQueryBuilder() {
        return searchManager.buildQueryBuilderForClass(CacheValue.class).get();
    }

    private ScoredDocument createScoredDocument(Document document) {
        ScoredDocument.Builder builder = ScoredDocument.newBuilder();
        builder.setId(document.getId());
        builder.setLocale(document.getLocale());
        builder.setOrderId(document.getOrderId());
        for (Field field : document.getFields()) {
            builder.addField(field);
        }
        return builder.build();
    }

    private org.apache.lucene.search.Query createLuceneQuery(Query query) {
        try {
            return new QueryParser(Version.LUCENE_35, CacheValue.ALL_FIELD_NAME, new StandardAnalyzer(Version.LUCENE_35)).parse(query.getQueryString());
        } catch (ParseException e) {
            throw new RuntimeException("Cannot parse query string: " + query.getQueryString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Results<ScoredDocument> newResults(OperationResult operationResult, Collection<ScoredDocument> results, long numberFound, int numberReturned, Cursor cursor) {
        return ReflectionUtils.newInstance(
            Results.class,
            new Class[]{OperationResult.class, Collection.class, long.class, int.class, Cursor.class},
            new Object[]{operationResult, results, numberFound, numberReturned, cursor}
        );
    }

    public ListResponse<Document> listDocuments(ListRequest listRequest) {
        List<Document> documents = new ArrayList<Document>();
        CacheQuery cacheQuery = searchManager.getQuery(createIndexAndNamespaceQuery());
        if (listRequest.getLimit() > 0) {
            cacheQuery.maxResults(listRequest.getLimit());
        }
        for (Object o : cacheQuery.list()) {
            CacheValue cacheValue = (CacheValue) o;
            documents.add(cacheValue.getDocument());
        }
        return newListResponse(documents);
    }

    @SuppressWarnings("unchecked")
    private ListResponse<Document> newListResponse(List<Document> documents) {
        return ReflectionUtils.newInstance(ListResponse.class, new Class[]{List.class}, new Object[]{documents});
    }

    public Schema getSchema() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
