package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

/**
 * Checks whether the query's filter contains a FilterPredicate on KEY_RESERVED_PROPERTY with a Key value in a namespace
 * that is not the same as the current namespace in which the query is being executed.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class FilterNamespaceChecker {


    public void checkNamespace(Query query) {
        checkNamespace(query.getFilter());

        //noinspection deprecation
        for (Query.FilterPredicate predicate : query.getFilterPredicates()) {
            checkNamespace(predicate);
        }
    }

    private void checkNamespace(Query.Filter filter) {
        if (filter instanceof Query.CompositeFilter) {
            Query.CompositeFilter compositeFilter = (Query.CompositeFilter) filter;
            for (Query.Filter subFilter : compositeFilter.getSubFilters()) {
                checkNamespace(subFilter);
            }
        } else if (filter instanceof Query.FilterPredicate) {
            Query.FilterPredicate predicate = (Query.FilterPredicate) filter;
            if (Entity.KEY_RESERVED_PROPERTY.equals(predicate.getPropertyName())) {
                String currentNamespace = NamespaceManager.get();
                if (predicate.getValue() instanceof Key) {
                    Key key = (Key) predicate.getValue();
                    if (isDifferent(currentNamespace, key.getNamespace())) {
                        throw new IllegalArgumentException(predicate.getPropertyName() + " filter namespace is " + key.getNamespace() + " but query namespace is " + currentNamespace);
                    }
                }
            }
        }
    }

    private boolean isDifferent(String currentNamespace, String keyNamespace) {
        return currentNamespace == null ? keyNamespace != null : !currentNamespace.equals(keyNamespace);
    }
}
