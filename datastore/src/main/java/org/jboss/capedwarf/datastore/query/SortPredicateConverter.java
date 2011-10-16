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

import com.google.appengine.api.datastore.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts GAE's Query.SortPredicates to Lucene's Sort.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class SortPredicateConverter {

    public Sort convert(List<Query.SortPredicate> sortPredicates) {
        return new Sort(toArray(convertToSortFields(sortPredicates)));
    }

    private SortField[] toArray(List<SortField> sortFields) {
        return sortFields.toArray(new SortField[sortFields.size()]);
    }

    private List<SortField> convertToSortFields(List<Query.SortPredicate> sortPredicates) {
        List<SortField> sortFields = new ArrayList<SortField>();
        for (Query.SortPredicate sortPredicate : sortPredicates) {
            sortFields.add(convertToSortField(sortPredicate));
        }
        return sortFields;
    }

    private SortField convertToSortField(Query.SortPredicate sortPredicate) {
        boolean reverse = sortPredicate.getDirection() == Query.SortDirection.DESCENDING;
        return new SortField(sortPredicate.getPropertyName(), SortField.STRING, reverse);   // TODO: find appropriate SortField type
    }
}
