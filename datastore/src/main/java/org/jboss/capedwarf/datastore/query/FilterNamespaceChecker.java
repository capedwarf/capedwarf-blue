package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

/**
 * Checks whether the query's filter contains a FilterPredicate on KEY_RESERVED_PROPERTY with a Key value in a namespace
 * that is not the same as the query's namespace in which the query is being executed.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class FilterNamespaceChecker {


    public void checkNamespace(Query query) {
        String queryNamespace = QueryUtils.getNamespace(query);
        checkNamespace(queryNamespace, query.getFilter());

        //noinspection deprecation
        for (Query.FilterPredicate predicate : query.getFilterPredicates()) {
            checkNamespace(queryNamespace, predicate);
        }
    }

    private void checkNamespace(String queryNamespace, Query.Filter filter) {
        if (filter instanceof Query.CompositeFilter) {
            Query.CompositeFilter compositeFilter = (Query.CompositeFilter) filter;
            for (Query.Filter subFilter : compositeFilter.getSubFilters()) {
                checkNamespace(queryNamespace, subFilter);
            }
        } else if (filter instanceof Query.FilterPredicate) {
            Query.FilterPredicate predicate = (Query.FilterPredicate) filter;
            if (Entity.KEY_RESERVED_PROPERTY.equals(predicate.getPropertyName())) {
                if (predicate.getValue() instanceof Key) {
                    Key key = (Key) predicate.getValue();
                    if (isDifferent(queryNamespace, key.getNamespace())) {
                        throw new IllegalArgumentException(predicate.getPropertyName() + " filter namespace is " + key.getNamespace() + " but query namespace is " + queryNamespace);
                    }
                }
            }
        }
    }

    private boolean isDifferent(String namespace1, String namespace2) {
        return namespace1 == null ? namespace2 != null : !namespace1.equals(namespace2);
    }
}
