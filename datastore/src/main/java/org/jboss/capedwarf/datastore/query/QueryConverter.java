package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;

/**
 * Converts a GAE query to Infinispan's CacheQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class QueryConverter {

    public static final String KIND_PROPERTY_KEY = "____capedwarf.entity.kind___";
    public static final String NAMESPACE_PROPERTY_KEY = "____capedwarf.entity.namespace___";
    public static final String ANCESTOR_PROPERTY_KEY = "____capedwarf.entity.ancestor.key___";

    private SearchManager searchManager;

    private FilterConverter filterConverter;
    private SortPredicateConverter sortPredicateConverter;

    public QueryConverter(SearchManager searchManager) {
        this.searchManager = searchManager;
        this.filterConverter = new FilterConverter(createQueryBuilder());
        this.sortPredicateConverter = new SortPredicateConverter();
    }

    private QueryBuilder createQueryBuilder() {
        return searchManager.buildQueryBuilderForClass(Entity.class).get();
    }

    public CacheQuery convert(Query gaeQuery) {
        CacheQuery cacheQuery = getCacheQuery(gaeQuery);
        addSortToQuery(cacheQuery, gaeQuery);
        return cacheQuery;
    }

    private CacheQuery getCacheQuery(Query gaeQuery) {
        CacheQuery cacheQuery = getCacheQuery(createLuceneQuery(gaeQuery));
        Projections.applyProjections(gaeQuery, cacheQuery);
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
        Query.Filter filter = getFilter(gaeQuery);
        return filterConverter.convert(filter);
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
        addNamespaceFilterPredicate(list, getNamespace(gaeQuery));
        addEntityKindFilterPredicate(list, gaeQuery.getKind());
        addAncestorFilterPredicate(list, gaeQuery.getAncestor());
        if (gaeQuery.getFilter() != null) {
            list.add(gaeQuery.getFilter());
        }
        list.addAll(gaeQuery.getFilterPredicates());
        return list;
    }

    private String getNamespace(Query gaeQuery) {
        Object appIdNamespace = ReflectionUtils.invokeInstanceMethod(gaeQuery, "getAppIdNamespace");
        return (String) ReflectionUtils.invokeInstanceMethod(appIdNamespace, "getNamespace");
    }

    private void addAncestorFilterPredicate(List<Query.Filter> list, Key ancestor) {
        if (ancestor != null) {
            list.add(new Query.FilterPredicate(ANCESTOR_PROPERTY_KEY, EQUAL, ancestor));
        }
    }

    private void addEntityKindFilterPredicate(List<Query.Filter> list, String kind) {
        if (kind != null) {
            list.add(new Query.FilterPredicate(KIND_PROPERTY_KEY, EQUAL, kind));
        }
    }

    private void addNamespaceFilterPredicate(List<Query.Filter> list, String namespace) {
        if (namespace != null) {
            list.add(new Query.FilterPredicate(NAMESPACE_PROPERTY_KEY, EQUAL, NamespaceBridge.objectToString(namespace)));
        }
    }

}
