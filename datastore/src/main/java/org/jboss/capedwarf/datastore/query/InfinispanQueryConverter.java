package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.query.SearchManager;

/**
 * Converts a GAE query to a Lucene query.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class InfinispanQueryConverter {

    private Query gaeQuery;
    private QueryBuilder queryBuilder;

    public InfinispanQueryConverter(SearchManager searchManager, Query gaeQuery) {
        this.gaeQuery = gaeQuery;
        this.queryBuilder = searchManager.buildQueryBuilderForClass(Entity.class).get();
    }


    public org.apache.lucene.search.Query convert() {
        if (gaeQuery.getFilterPredicates().isEmpty()) {
            return queryBuilder.all().createQuery();
        }

        if (gaeQuery.getFilterPredicates().size() == 1) {
            return createSubQuery(gaeQuery.getFilterPredicates().get(0));
        }

        BooleanJunction<BooleanJunction> booleanJunction = queryBuilder.bool();
        for (Query.FilterPredicate filterPredicate : gaeQuery.getFilterPredicates()) {
            booleanJunction.must(createSubQuery(filterPredicate));
        }
        return booleanJunction.createQuery();
    }

    private org.apache.lucene.search.Query createSubQuery(Query.FilterPredicate filterPredicate) {
        String propertyName = filterPredicate.getPropertyName();
        Object propertyValue = filterPredicate.getValue();

        switch (filterPredicate.getOperator()) {
            case EQUAL:
//                return queryBuilder.keyword().onField(propertyName).matching(propertyValue).createQuery();
                return queryBuilder.phrase().onField(propertyName).sentence((String) propertyValue).createQuery();
            case GREATER_THAN:
                return queryBuilder.range().onField(propertyName).above(propertyValue).createQuery();
            case LESS_THAN:
                return queryBuilder.range().onField(propertyName).below(propertyValue).createQuery();
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
    }


}
