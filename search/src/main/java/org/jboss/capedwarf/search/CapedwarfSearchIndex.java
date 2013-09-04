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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.OperationResult;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.Schema;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.StatusCode;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeTerminationExcludable;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("deprecation")
public class CapedwarfSearchIndex implements Index {
    private final static Function<Document.Builder, Document> FN = new Function<Document.Builder, Document>() {
        public Document apply(Document.Builder input) {
            return input.build();
        }
    };

    private final Logger log = Logger.getLogger(getClass().getName());

    private String name;
    private String namespace;

    private Cache<CacheKey, CacheValue> cache;
    private SearchManager searchManager;

    private SchemaAdapter schemaAdapter;

    public CapedwarfSearchIndex(String name, String namespace, Cache<CacheKey, CacheValue> cache) {
        this.name = name;
        this.namespace = namespace;
        this.cache = cache;
        this.searchManager = Search.getSearchManager(cache);
        this.schemaAdapter = new CapedwarfSchema(getSchemaName(name, namespace));
    }

    private String getSchemaName(String name, String namespace) {
        return "__Schema__#" + name + "#" + normalizeNamespace(namespace);
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public Future<Void> removeAsync(String... documentIds) {
        return removeAsync(Arrays.asList(documentIds));
    }

    public Future<Void> removeAsync(final Iterable<String> documentIds) {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                remove(documentIds);
                return null;
            }
        });
    }

    public Future<Results<ScoredDocument>> searchAsync(String queryString) {
        return searchAsync(Query.newBuilder().build(queryString));
    }

    public Future<Results<ScoredDocument>> searchAsync(final Query query) {
        return ExecutorFactory.wrap(new Callable<Results<ScoredDocument>>() {
            public Results<ScoredDocument> call() throws Exception {
                return search(query);
            }
        });
    }

    public void remove(String... documentIds) {
        remove(Arrays.asList(documentIds));
    }

    public void remove(Iterable<String> documentIds) {
        delete(documentIds);
    }

    public Document get(String documentId) {
        CacheValue value = cache.get(getCacheKey(documentId));
        return (value != null ? value.getDocument() : null);
    }

    private CacheKey getCacheKey(String documentId) {
        return new CacheKey(getName(), getNamespace(), documentId);
    }

    private CacheValue getCacheValue(Document document) {
        return new CacheValue(getName(), getNamespace(), document);
    }

    @SuppressWarnings("UnusedParameters")
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
                .must(createLuceneQuery(query))
                .createQuery());

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
        copyPropertiesToBuilder(document, builder);
        return builder.build();
    }

    private Document createCopyWithId(Document document, String id) {
        Document.Builder builder = Document.newBuilder();
        builder.setId(id);
        copyPropertiesToBuilder(document, builder);
        return builder.build();
    }

    private void copyPropertiesToBuilder(Document document, Document.Builder builder) {
        builder.setId(document.getId());
        builder.setLocale(document.getLocale());
        builder.setRank(document.getRank());
        for (Field field : document.getFields()) {
            builder.addField(field);
        }
    }

    private org.apache.lucene.search.Query createLuceneQuery(Query query) {
        QueryConverter queryConverter = new QueryConverter(CacheValue.ALL_FIELD_NAME) {
            @Override
            protected GAEQueryTreeVisitor createTreeVisitor(String allFieldName) {
                return new MultiFieldGAEQueryTreeVisitor(allFieldName);
            }
        };
        org.apache.lucene.search.Query luceneQuery = queryConverter.convert(query.getQueryString());
        log.info("luceneQuery = " + luceneQuery);
        return luceneQuery;
    }

    @SuppressWarnings("unchecked")
    private Results<ScoredDocument> newResults(OperationResult operationResult, Collection<ScoredDocument> results, long numberFound, int numberReturned, Cursor cursor) {
        return new Results<ScoredDocument>(operationResult, results, numberFound, numberReturned, cursor){};
    }

    private CacheQuery createListDocumentsQuery(GetRequest request) {
        CacheQuery query;
        if (request.getStartId() == null) {
            query = searchManager.getQuery(createIndexAndNamespaceQuery());
        } else {
            query = searchManager.getQuery(
                createQueryBuilder().bool()
                    .must(createIndexAndNamespaceQuery())
                    .must(createStartingIdQuery(request))
                    .createQuery());
        }
        if (request.getLimit() > 0) {
            query.maxResults(request.getLimit());
        }
        return query;
    }

    private org.apache.lucene.search.Query createStartingIdQuery(GetRequest request) {
        RangeTerminationExcludable range = createQueryBuilder().range().onField(CacheValue.ID_FIELD_NAME).above(request.getStartId());
        if (!request.isIncludeStart()) {
            range = range.excludeLimit();
        }
        return range.createQuery();
    }

    public Schema getSchema() {
        return schemaAdapter.buildSchema();
    }

    public Future<Void> deleteSchemaAsync() {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                deleteSchema();
                return null;
            }
        });
    }

    public void deleteSchema() {
        schemaAdapter.deleteSchema();
    }

    public Future<Void> deleteAsync(String... strings) {
        return deleteAsync(Arrays.asList(strings));
    }

    public Future<Void> deleteAsync(final Iterable<String> strings) {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delete(strings);
                return null;
            }
        });
    }

    public Future<PutResponse> putAsync(Document... documents) {
        return putAsync(Arrays.asList(documents));
    }

    public Future<PutResponse> putAsync(final Document.Builder... builders) {
        return ExecutorFactory.wrap(new Callable<PutResponse>() {
            public PutResponse call() throws Exception {
                return put(builders);
            }
        });
    }

    public Future<PutResponse> putAsync(final Iterable<Document> documents) {
        return ExecutorFactory.wrap(new Callable<PutResponse>() {
            public PutResponse call() throws Exception {
                return put(documents);
            }
        });
    }

    public Future<GetResponse<Document>> getRangeAsync(final GetRequest getRequest) {
        return ExecutorFactory.wrap(new Callable<GetResponse<Document>>() {
            public GetResponse<Document> call() throws Exception {
                return getRange(getRequest);
            }
        });
    }

    public Future<GetResponse<Document>> getRangeAsync(final GetRequest.Builder builder) {
        return ExecutorFactory.wrap(new Callable<GetResponse<Document>>() {
            public GetResponse<Document> call() throws Exception {
                return getRange(builder);
            }
        });
    }

    public void delete(String... strings) {
        delete(Arrays.asList(strings));
    }

    public void delete(Iterable<String> documentIds) {
        for (String documentId : documentIds) {
            CacheValue cacheValue = cache.remove(getCacheKey(documentId));
            if (cacheValue != null) {
                Document document = cacheValue.getDocument();
                schemaAdapter.removeFields(document.getFieldNames());
            }
        }
    }

    public PutResponse put(Document... documents) {
        return put(Arrays.asList(documents));
    }

    public PutResponse put(Document.Builder... builders) {
        return put(toDocuments(builders));
    }

    public PutResponse put(Iterable<Document> documents) {
        final List<OperationResult> results = new ArrayList<OperationResult>();
        final List<String> ids = new ArrayList<String>();
        final SetMultimap<String, Field.FieldType> fields = HashMultimap.create();

        for (Document document : documents) {
            StatusCode status = StatusCode.OK;
            String errorDetail = null;
            String id = null;
            try {
                Document documentWithId = document;
                if (document.getId() == null) {
                    documentWithId = createCopyWithId(document, generateId(document));
                }
                cache.put(getCacheKey(documentWithId.getId()), getCacheValue(documentWithId));
                id = documentWithId.getId();
            } catch (Exception e) {
                // TODO -- check err
                status = StatusCode.INTERNAL_ERROR;
                errorDetail = e.getMessage();
            }
            results.add(new OperationResult(status, errorDetail));
            ids.add(id);

            for (Field field : document.getFields()) {
                fields.put(field.getName(), field.getType());
            }
        }

        schemaAdapter.addFields(fields);

        return new PutResponse(results, ids){};
    }

    public GetResponse<Document> getRange(GetRequest request) {
        final List<Document> documents = new ArrayList<Document>();
        final CacheQuery cacheQuery = createListDocumentsQuery(request);
        for (Object o : cacheQuery.list()) {
            CacheValue cacheValue = (CacheValue) o;
            documents.add(cacheValue.getDocument());
        }
        return new GetResponse<Document>(documents){};
    }

    public GetResponse<Document> getRange(GetRequest.Builder builder) {
        return getRange(builder.build());
    }

    protected static List<Document> toDocuments(Document.Builder... builders) {
        return Lists.transform(Lists.newArrayList(builders), FN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CapedwarfSearchIndex that = (CapedwarfSearchIndex) o;

        if (!name.equals(that.name)) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CapedwarfSearchIndex{" +
            "name='" + name + '\'' +
            ", namespace='" + namespace + '\'' +
            '}';
    }
}
