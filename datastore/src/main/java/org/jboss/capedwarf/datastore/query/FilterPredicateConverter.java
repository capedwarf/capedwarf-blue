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

import java.util.Collection;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;

/**
 * Converts GAE's Query.FilterPredicates to Lucene Queries
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class FilterPredicateConverter {

    private QueryBuilder queryBuilder;

    public FilterPredicateConverter(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public Query convert(Collection<com.google.appengine.api.datastore.Query.FilterPredicate> filterPredicates) {
        if (filterPredicates.isEmpty()) {
            return queryBuilder.all().createQuery();
        }
        BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
        for (com.google.appengine.api.datastore.Query.FilterPredicate filterPredicate : filterPredicates) {
            bool.must(convert(filterPredicate));
        }
        return bool.createQuery();
    }

    public Query convert(com.google.appengine.api.datastore.Query.FilterPredicate filterPredicate) {
        String fieldName = filterPredicate.getPropertyName();
        Object value = filterPredicate.getValue();

        switch (filterPredicate.getOperator()) {
            case EQUAL:
                return equal(fieldName, value);
            case NOT_EQUAL:
                return not(equal(fieldName, value));
            case IN:
                return in(fieldName, (Collection<?>) value);
            case GREATER_THAN:
                return greaterThan(fieldName, value);
            case GREATER_THAN_OR_EQUAL:
                return greaterThanOrEqual(fieldName, value);
            case LESS_THAN:
                return lessThan(fieldName, value);
            case LESS_THAN_OR_EQUAL:
                return lessThanOrEqual(fieldName, value);
            default:
                throw new IllegalArgumentException("Unknown operator " + filterPredicate.getOperator());
        }
    }

    private Query in(String fieldName, Collection<?> values) {
        BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
        for (Object value : values) {
            bool.should(equal(fieldName, value));
        }
        return bool.createQuery();
    }

    private Query not(Query query) {
        return queryBuilder.bool().must(query).not().createQuery();
    }

    public Query equal(String fieldName, Object value) {
        return keywordOnField(fieldName)
                .matching(convertToString(value))
                .createQuery();
    }

    private TermMatchingContext keywordOnField(String fieldName) {
        return queryBuilder
                .keyword().onField(fieldName)
                .ignoreFieldBridge()
                .ignoreAnalyzer();
    }

    private Query greaterThan(String fieldName, Object value) {
        return rangeOnField(fieldName)
                .above(convertToString(value)).excludeLimit()
                .createQuery();
    }

    private Query greaterThanOrEqual(String fieldName, Object value) {
        return rangeOnField(fieldName)
                .above(convertToString(value))
                .createQuery();
    }

    private Query lessThan(String fieldName, Object value) {
        return rangeOnField(fieldName)
                .below(convertToString(value)).excludeLimit()
                .createQuery();
    }

    private Query lessThanOrEqual(String fieldName, Object value) {
        return rangeOnField(fieldName)
                .below(convertToString(value))
                .createQuery();
    }

    private RangeMatchingContext rangeOnField(String fieldName) {
        return queryBuilder
                .range().onField(fieldName)
                .ignoreFieldBridge()
                .ignoreAnalyzer();
    }

    private String convertToString(Object value) {
        return Bridge.matchBridge(value).objectToString(value);
    }
}
