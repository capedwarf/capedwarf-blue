package org.jboss.capedwarf.datastore.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.datastore.stats.StatsQueryTypeFactory;
import org.jboss.capedwarf.shared.config.IndexesXml;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class Indexes {

    private static final Set<String> KEY_RESERVED_PROPERTY_AS_SET = Collections.singleton(Entity.KEY_RESERVED_PROPERTY);

    public static IndexesXml.Index getIndex(Query query) {
        for (IndexesXml.Index index : CapedwarfEnvironment.getThreadLocalInstance().getIndexes().getIndexes().values()) {
            if (indexMatches(index, query)) {
                return index;
            }
        }

        if (needsExplicitlyDefinedIndex(query)) {
            throw newDatastoreNeedIndexException("No matching index found", createIndexXml(query));
        } else {
            return null;
        }
    }

    private static boolean needsExplicitlyDefinedIndex(Query query) {
        if (isStatsQuery(query)) {
            return false;
        }

        if (query.getSortPredicates().size() > 1) {
            return true;
        }

        Set<String> sortOrders = new HashSet<>();
        for (Query.SortPredicate sort : query.getSortPredicates()) {
            if (isDescendingKeySort(sort)) {
                return true;
            }
            sortOrders.add(sort.getPropertyName());
        }

        Set<String> inequalityFilters = new HashSet<>();
        Set<String> equalityFilters = new HashSet<>();
        for (Query.FilterPredicate predicate : getFilterPredicates(query)) {
            Set<String> set = isEqualityFilter(predicate) ? equalityFilters : inequalityFilters;
            set.add(predicate.getPropertyName());
        }

        Set<String> nonKeyInequalityFilters = Sets.difference(inequalityFilters, KEY_RESERVED_PROPERTY_AS_SET);
        Set<String> nonKeySortOrders = Sets.difference(sortOrders, KEY_RESERVED_PROPERTY_AS_SET);

        Set<String> otherNonKeySortProperties = Sets.difference(nonKeySortOrders, equalityFilters);
        if (has(equalityFilters) && has(otherNonKeySortProperties)) {
            return true;
        }

        if (isAncestorQuery(query)) {
            return has(nonKeyInequalityFilters) || has(nonKeySortOrders);
        } else {
            return has(nonKeyInequalityFilters) && has(equalityFilters);
        }
    }

    private static boolean isStatsQuery(Query query) {
        return StatsQueryTypeFactory.isStatsKind(query.getKind());
    }

    private static boolean has(Set<String> set) {
        return !set.isEmpty();
    }

    private static boolean isDescendingKeySort(Query.SortPredicate sort) {
        return sort.getPropertyName().equals(Entity.KEY_RESERVED_PROPERTY)
            && sort.getDirection() == Query.SortDirection.DESCENDING;
    }

    private static boolean isEqualityFilter(Query.FilterPredicate filter) {
        return filter.getOperator() == Query.FilterOperator.EQUAL
            || filter.getOperator() == Query.FilterOperator.IN;
    }

    private static boolean isAncestorQuery(Query query) {
        return query.getAncestor() != null;
    }

    private static DatastoreNeedIndexException newDatastoreNeedIndexException(String message, String missingIndexDefinitionXml) {
        DatastoreNeedIndexException ex = new DatastoreNeedIndexException(message);
        ReflectionUtils.invokeInstanceMethod(ex, "setMissingIndexDefinitionXml", String.class, missingIndexDefinitionXml);
        return ex;
    }

    private static String createIndexXml(Query query) {
        StringBuilder sb = new StringBuilder();
        sb.append("<datastore-index kind=\"").append(query.getKind()).append("\" ancestor=\"").append(query.getAncestor() != null).append("\" source=\"manual\">\n");
        for (String property : getIndexProperties(query)) {
            sb.append("  <property name=\"").append(property).append("\" direction=\"asc\"/>\n"); // TODO: proper direction (when necessary)
        }
        sb.append("</datastore-index>\n");
        return sb.toString();
    }

    private static List<String> getIndexProperties(Query query) {
        Set<String> filterProperties = getFilterProperties(query);
        Set<String> sortProperties = getSortProperties(query);
        Set<String> projectionProperties = getProjectionProperties(query);
        removeDuplicates(filterProperties, sortProperties, projectionProperties);

        List<String> properties = new ArrayList<>();
        properties.addAll(filterProperties);
        properties.addAll(sortProperties);
        properties.addAll(projectionProperties);
        return properties;
    }

    private static boolean indexMatches(IndexesXml.Index index, Query query) {
        if (!index.getKind().equals(query.getKind())) {
            return false;
        }

        Set<String> filterProperties = getFilterProperties(query);
        Set<String> sortProperties = getSortProperties(query);
        Set<String> projectionProperties = getProjectionProperties(query);
        removeDuplicates(filterProperties, sortProperties, projectionProperties);

        List<String> indexProperties = index.getPropertyNames();

        while (!indexProperties.isEmpty()) {
            String property = indexProperties.get(0);
            if (!filterProperties.isEmpty()) {
                if (filterProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else if (!sortProperties.isEmpty()) {
                if (sortProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else if (!projectionProperties.isEmpty()) {
                if (projectionProperties.remove(property)) {
                    indexProperties.remove(0);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return indexProperties.isEmpty() && filterProperties.isEmpty() && sortProperties.isEmpty() && projectionProperties.isEmpty();
    }

    private static void removeDuplicates(Set<String> filterProperties, Set<String> sortProperties, Set<String> projectionProperties) {
        sortProperties.removeAll(filterProperties);
        projectionProperties.removeAll(filterProperties);
        projectionProperties.removeAll(sortProperties);
    }

    private static Set<String> getFilterProperties(Query query) {
        Set<String> set = new HashSet<>();
        for (Query.FilterPredicate predicate : getFilterPredicates(query)) {
            set.add(predicate.getPropertyName());
        }
        return set;
    }

    @SuppressWarnings("deprecation")
    private static Set<Query.FilterPredicate> getFilterPredicates(Query query) {
        Set<Query.FilterPredicate> set = new HashSet<>();
        addFilterPredicatesToSet(set, query.getFilter());
        for (Query.FilterPredicate predicate : query.getFilterPredicates()) {
            addFilterPredicatesToSet(set, predicate);
        }
        return set;
    }

    private static void addFilterPredicatesToSet(Set<Query.FilterPredicate> set, Query.Filter filter) {
        if (filter == null) {
            return;
        }
        if (filter instanceof Query.FilterPredicate) {
            Query.FilterPredicate predicate = (Query.FilterPredicate) filter;
            set.add(predicate);
        } else if (filter instanceof Query.CompositeFilter) {
            Query.CompositeFilter composite = (Query.CompositeFilter) filter;
            for (Query.Filter subFilter : composite.getSubFilters()) {
                addFilterPredicatesToSet(set, subFilter);
            }
        } else {
            throw new IllegalArgumentException("Unsupported filter type " + filter);
        }
    }

    private static Set<String> getSortProperties(Query query) {
        Set<String> set = new HashSet<>();
        for (Query.SortPredicate sortPredicate : query.getSortPredicates()) {
            set.add(sortPredicate.getPropertyName());
        }
        return set;
    }

    private static Set<String> getProjectionProperties(Query query) {
        Set<String> set = new HashSet<>();
        for (Projection projection : query.getProjections()) {
            set.add(Projections.getPropertyName(projection));
        }
        return set;
    }
}
