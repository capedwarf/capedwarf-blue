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

package org.jboss.capedwarf.datastore;

import javax.transaction.Status;

import com.google.appengine.api.datastore.Query;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class LazyChecker {
    protected final Query query;
    protected final boolean inTx;

    protected LazyChecker(Query query, boolean inTx) {
        this.query = query;
        this.inTx = inTx;
        register();
    }

    protected void register() {
        if (inTx) {
            JBossDatastoreService.registerKey(query.getAncestor());
        }
    }

    protected void check() {
        checkInequalityConstraints(query);

        if (inTx) {
            if (JBossTransaction.getTxStatus() != Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction with which this operation is associated is not active.");
            }

            JBossDatastoreService.checkKeys();
        }
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

}
