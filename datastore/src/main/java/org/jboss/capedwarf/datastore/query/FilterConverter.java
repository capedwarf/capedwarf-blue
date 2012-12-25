/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

import static com.google.appengine.api.datastore.Query.CompositeFilter;
import static com.google.appengine.api.datastore.Query.Filter;
import static com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Converts GAE's Query.FilterPredicates to Lucene Queries
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class FilterConverter {

    private LuceneQueryBuilder queryBuilder;

    public FilterConverter(LuceneQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public Query convert(Filter filter) {
        if (filter == null) {
            return queryBuilder.matchAll();
        }

        if (filter instanceof CompositeFilter) {
            CompositeFilter compositeFilter = (CompositeFilter) filter;
            return convert(compositeFilter);
        } else if (filter instanceof FilterPredicate) {
            FilterPredicate predicate = (FilterPredicate) filter;
            return convert(predicate);
        } else {
            throw new IllegalArgumentException("Unknown filter type: " + filter);
        }
    }

    private Query convert(CompositeFilter compositeFilter) {
        List<Query> subQueries = new ArrayList<Query>(compositeFilter.getSubFilters().size());
        for (Filter subFilter : compositeFilter.getSubFilters()) {
            subQueries.add(convert(subFilter));
        }

        switch (compositeFilter.getOperator()) {
            case AND:
                return queryBuilder.all(subQueries);
            case OR:
                return queryBuilder.any(subQueries);
            default:
                throw new IllegalArgumentException("Unknown operator " + compositeFilter.getOperator());
        }
    }

    public Query convert(FilterPredicate filterPredicate) {
        String fieldName = filterPredicate.getPropertyName();
        Object value = filterPredicate.getValue();

        switch (filterPredicate.getOperator()) {
            case EQUAL:
                return queryBuilder.equal(fieldName, value);
            case NOT_EQUAL:
                return queryBuilder.notEqual(fieldName, value);
            case IN:
                return queryBuilder.in(fieldName, (Collection<?>) value);
            case GREATER_THAN:
                return queryBuilder.greaterThan(fieldName, value);
            case GREATER_THAN_OR_EQUAL:
                return queryBuilder.greaterThanOrEqual(fieldName, value);
            case LESS_THAN:
                return queryBuilder.lessThan(fieldName, value);
            case LESS_THAN_OR_EQUAL:
                return queryBuilder.lessThanOrEqual(fieldName, value);
            default:
                throw new IllegalArgumentException("Unknown operator " + filterPredicate.getOperator());
        }
    }

}
