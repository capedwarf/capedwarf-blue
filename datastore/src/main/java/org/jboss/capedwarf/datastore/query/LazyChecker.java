/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.infinispan.query.CacheQuery;
import org.jboss.capedwarf.datastore.LazyKeyChecker;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LazyChecker extends LazyKeyChecker {
    protected final QueryHolder holder;
    protected final FetchOptions fetchOptions;

    public LazyChecker(QueryHolder holder, FetchOptions fetchOptions) {
        super(holder.getQuery().getAncestor(), holder.isInTx());
        this.holder = holder;
        this.fetchOptions = fetchOptions;
    }

    protected void apply() {
        final CacheQuery cacheQuery = holder.getCacheQuery();
        final Integer offset = fetchOptions.getOffset();
        if (offset != null) {
            cacheQuery.firstResult(offset);
        }
        final Integer limit = fetchOptions.getLimit();
        if (limit != null) {
            cacheQuery.maxResults(limit);
        }
        final Cursor start = fetchOptions.getStartCursor();
        if (start != null) {
            JBossCursorHelper.applyStartCursor(start, cacheQuery);
        }
        final Cursor end = fetchOptions.getEndCursor();
        if (end != null) {
            JBossCursorHelper.applyEndCursor(end, cacheQuery, start);
        }
    }

    @Override
    protected void check() {
        checkInequalityConstraints();
        super.check();
    }

    @SuppressWarnings("deprecation")
    private void checkInequalityConstraints() {
        final Query query = holder.getQuery();
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
}
