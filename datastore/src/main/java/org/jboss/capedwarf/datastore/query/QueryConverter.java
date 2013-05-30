package org.jboss.capedwarf.datastore.query;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.search.Sort;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.shared.config.IndexesXml;

/**
 * Converts a GAE query to Infinispan's CacheQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class QueryConverter {

    public static final String KIND_PROPERTY_KEY = "____capedwarf.entity.kind___";
    public static final String NAMESPACE_PROPERTY_KEY = "____capedwarf.entity.namespace___";
    public static final String ANCESTOR_PROPERTY_KEY = "____capedwarf.entity.ancestor.key___";

    private LuceneQueryBuilder queryBuilder;
    private SearchManager searchManager;

    private FilterConverter filterConverter;
    private SortPredicateConverter sortPredicateConverter;

    public QueryConverter(SearchManager searchManager) {
        this.searchManager = searchManager;
        this.queryBuilder = new LuceneQueryBuilder(searchManager.buildQueryBuilderForClass(Entity.class).get());
        this.filterConverter = new FilterConverter(queryBuilder);
        this.sortPredicateConverter = new SortPredicateConverter();
    }

    public CacheQuery convert(Query gaeQuery) {
        CacheQuery cacheQuery = getCacheQuery(gaeQuery);
        addSortToQuery(cacheQuery, gaeQuery);
        return cacheQuery;
    }

    private CacheQuery getCacheQuery(Query gaeQuery) {
        CacheQuery cacheQuery = getCacheQuery(createLuceneQuery(gaeQuery));
        IndexesXml.Index index = Indexes.getIndex(gaeQuery);
        Projections.applyProjections(gaeQuery, cacheQuery, index);
        return cacheQuery;
    }

    private void addSortToQuery(CacheQuery cacheQuery, Query gaeQuery) {
        List<Query.SortPredicate> sortPredicates = gaeQuery.getSortPredicates();
        if (!sortPredicates.isEmpty()) {
            Sort sort = sortPredicateConverter.convert(sortPredicates);
            cacheQuery.sort(sort);
        }
    }

    private CacheQuery getCacheQuery(org.apache.lucene.search.Query luceneQuery) {
        return searchManager.getQuery(luceneQuery, Entity.class);
    }

    private org.apache.lucene.search.Query createLuceneQuery(Query gaeQuery) {
        return queryBuilder.all(getQueryList(gaeQuery));
    }

    private List<org.apache.lucene.search.Query> getQueryList(Query gaeQuery) {
        List<org.apache.lucene.search.Query> list = new ArrayList<org.apache.lucene.search.Query>();
        addFilterQuery(list, gaeQuery);
        addNamespaceQuery(list, QueryUtils.getNamespace(gaeQuery));
        addEntityKindQuery(list, gaeQuery.getKind());
        addAncestorQuery(list, gaeQuery.getAncestor());
        addProjectionsFilterQuery(list, gaeQuery);
        return list;
    }

    private void addProjectionsFilterQuery(List<org.apache.lucene.search.Query> list, Query gaeQuery) {
        for (Projection projection : gaeQuery.getProjections()) {
            list.add(queryBuilder.notEqual(projection.getName(), Bridge.NullBridge.NULL_TOKEN));
        }
    }

    private void addFilterQuery(List<org.apache.lucene.search.Query> list, Query gaeQuery) {
        Query.Filter filter = getFilter(gaeQuery);
        list.add(filterConverter.convert(filter));
    }

    private Query.Filter getFilter(Query gaeQuery) {
        List<Query.Filter> filters = getAllFilterPredicates(gaeQuery);
        if (filters.size() == 1) {
            return filters.get(0);
        } else if (filters.size() > 1) {
            return Query.CompositeFilterOperator.and(filters);
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private List<Query.Filter> getAllFilterPredicates(Query gaeQuery) {
        List<Query.Filter> list = new ArrayList<Query.Filter>();
        if (gaeQuery.getFilter() != null) {
            list.add(gaeQuery.getFilter());
        }
        list.addAll(gaeQuery.getFilterPredicates());
        return list;
    }

    private void addAncestorQuery(List<org.apache.lucene.search.Query> list, Key ancestor) {
        if (ancestor != null) {
            list.add(equal(ANCESTOR_PROPERTY_KEY, Bridge.KEY.objectToString(ancestor)));
        }
    }

    private void addEntityKindQuery(List<org.apache.lucene.search.Query> list, String kind) {
        if (kind != null) {
            list.add(equal(KIND_PROPERTY_KEY, kind));
        }
    }

    private void addNamespaceQuery(List<org.apache.lucene.search.Query> list, String namespace) {
        if (namespace != null) {
            list.add(equal(NAMESPACE_PROPERTY_KEY, NamespaceBridge.objectToString(namespace)));
        }
    }

    public org.apache.lucene.search.Query equal(String fieldName, String value) {
        return queryBuilder.equal(fieldName, value);
    }

}
