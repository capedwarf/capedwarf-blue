package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.SearchManager;

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
    public static final String ANCESTOR_PROPERTY_KEY = "____capedwarf.entity.ancestor.key___";

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
        checkInequalityConstraints(gaeQuery);
        CacheQuery cacheQuery = getCacheQuery(createLuceneQuery(gaeQuery));
        addSortToQuery(cacheQuery, gaeQuery);
        return cacheQuery;
    }

    private void checkInequalityConstraints(Query query) {
        String inequalityFilterProperty = null;
        for (Query.FilterPredicate predicate : query.getFilterPredicates()) {
            if (isInequalityOperator(predicate.getOperator())) {
                if (inequalityFilterProperty == null) {
                    inequalityFilterProperty = predicate.getPropertyName();
                } else {
                    if (!inequalityFilterProperty.equals(predicate.getPropertyName())) {
                        throw new IllegalArgumentException("Only one inequality filter per query is supported.  " +
                            "Encountered both " + inequalityFilterProperty + " and " + predicate.getPropertyName());
                    }
                }
            }
        }

        if (inequalityFilterProperty != null && !query.getSortPredicates().isEmpty()) {
            Query.SortPredicate firstSortPredicate = query.getSortPredicates().get(0);
            String firstSortProperty = firstSortPredicate.getPropertyName();
            if (!firstSortProperty.equals(inequalityFilterProperty)) {
                throw new IllegalArgumentException("The first sort property must be the same as the property to which the " +
                    "inequality filter is applied.  In your query the first sort property is " + firstSortProperty + " " +
                    "but the inequality filter is on " + inequalityFilterProperty);
            }
        }
    }

    private boolean isInequalityOperator(Query.FilterOperator operator) {
        return operator != Query.FilterOperator.EQUAL && operator != Query.FilterOperator.IN;
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
        addEntityKindFilterPredicate(list, gaeQuery.getKind());
        addAncestorFilterPredicate(list, gaeQuery.getAncestor());
        list.addAll(gaeQuery.getFilterPredicates());
        return list;
    }

    private void addAncestorFilterPredicate(List<Query.FilterPredicate> list, Key ancestor) {
        if (ancestor != null) {
            list.add(new Query.FilterPredicate(ANCESTOR_PROPERTY_KEY, EQUAL, ancestor));
        }
    }

    private void addEntityKindFilterPredicate(List<Query.FilterPredicate> list, String kind) {
        if (kind != null) {
            list.add(new Query.FilterPredicate(KIND_PROPERTY_KEY, EQUAL, kind));
        }
    }


}
