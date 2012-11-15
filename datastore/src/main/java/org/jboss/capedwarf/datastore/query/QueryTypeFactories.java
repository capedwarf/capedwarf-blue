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

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

/**
 * Query type factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class QueryTypeFactories {
    private final List<QueryTypeFactory> factories = new ArrayList<QueryTypeFactory>();
    private final QueryHandleService service;

    /**
     * Is stats, metadata, ...
     *
     * @param kind the kind
     * @return true if special, false otherwise
     */
    public static boolean isSpecialKind(String kind) {
        return StatsQueryTypeFactory.isStatsKind(kind);
    }

    public QueryTypeFactories(QueryHandleService service) {
        this.service = service;
        // query type factories
        factories.add(new StatsQueryTypeFactory());
        factories.add(MetadataQueryTypeFactory.INSTANCE);
        factories.add(DefaultQueryTypeFactory.INSTANCE);
        // initialize
        for (QueryTypeFactory factory : factories) {
            factory.initialize(service);
        }
    }

    public PreparedQuery prepare(Transaction tx, Query query) {
        for (QueryTypeFactory factory : factories) {
            if (factory.handleQuery(tx, query)) {
                QueryHandle handle = factory.createQueryHandle(service);
                return handle.createQuery(tx, query);
            }
        }
        throw new IllegalArgumentException("No matching query type: " + query);
    }
}