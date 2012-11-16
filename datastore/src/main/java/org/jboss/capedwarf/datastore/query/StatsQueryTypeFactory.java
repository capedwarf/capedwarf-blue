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

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.capedwarf.common.compatibility.Compatibility;

/**
 * Query type factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class StatsQueryTypeFactory implements QueryTypeFactory {
    private QueryHandle queryHandle;

    static boolean isStatsKind(String kind) {
        return kind != null && kind.startsWith("__Stat_") && kind.endsWith("__");
    }

    protected boolean isEager() {
        Compatibility compatibility = Compatibility.getInstance();
        return compatibility.isEnabled(Compatibility.Feature.ENABLE_EAGER_DATASTORE_STATS);
    }

    public void initialize(QueryHandleService service) {
        if (isEager()) {
            EagerStatsQueryHandle esqh = new EagerStatsQueryHandle(service);
            esqh.initialize(service);
            queryHandle = esqh;
        } else {
            queryHandle = new OnDemandStatsQueryHandle(service);
        }
    }

    public boolean handleQuery(Transaction tx, Query query) {
        return isStatsKind(query.getKind());
    }

    public QueryHandle createQueryHandle(QueryHandleService service) {
        return queryHandle;
    }
}