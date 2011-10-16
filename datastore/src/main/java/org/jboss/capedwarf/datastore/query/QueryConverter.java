package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a GAE query to Infinispan's CacheQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class QueryConverter {

    private SearchManager searchManager;

    private FilterPredicateConverter filterPredicateConverter;
    private SortPredicateConverter sortPredicateConverter;

    public QueryConverter(SearchManager searchManager) {
        this.searchManager = searchManager;
        this.filterPredicateConverter = new FilterPredicateConverter(createQueryBuilder());
        this.sortPredicateConverter = new SortPredicateConverter();
    }

    private QueryBuilder createQueryBuilder() {
        return searchManager.buildQueryBuilderForClass(Entity.class).get();
    }

    public CacheQuery convert(Query gaeQuery) {
        CacheQuery cacheQuery = getCacheQuery(createLuceneQuery(gaeQuery));
        addSortToQuery(cacheQuery, gaeQuery);
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
        return filterPredicateConverter.convert(getAllFilterPredicates(gaeQuery));
    }

    private List<Query.FilterPredicate> getAllFilterPredicates(Query gaeQuery) {
        List<Query.FilterPredicate> list = new ArrayList<Query.FilterPredicate>();
        list.add(createEntityKindFilterPredicate(gaeQuery));
        list.addAll(gaeQuery.getFilterPredicates());
        return list;
    }

    private Query.FilterPredicate createEntityKindFilterPredicate(Query gaeQuery) {
        return new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, gaeQuery.getKind());
    }


}
