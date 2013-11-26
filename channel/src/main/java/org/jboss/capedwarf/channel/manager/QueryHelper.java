/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.channel.manager;

import java.util.List;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermTermination;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class QueryHelper {
    private final SearchManager searchManager;

    QueryHelper(Cache<String, ?> channels) {
        searchManager = Search.getSearchManager(channels);
    }

    static TermTermination toTerm(QueryBuilder builder, String field, Object value) {
        return builder.keyword().onField(field).ignoreAnalyzer().ignoreFieldBridge().matching(value);
    }

    @SuppressWarnings("unchecked")
    List<Channel> getChannels(String clientId) {
        QueryBuilder builder = searchManager.buildQueryBuilderForClass(ChannelImpl.class).get();
        Query luceneQuery = builder.bool().must(toTerm(builder, ChannelImpl.CLIENT_ID, clientId).createQuery()).createQuery();
        CacheQuery query = searchManager.getQuery(luceneQuery, ChannelImpl.class);
        return (List<Channel>) (List) query.list();
    }

    @SuppressWarnings("unchecked")
    List<Message> getPendingMessages(String token) {
        QueryBuilder builder = searchManager.buildQueryBuilderForClass(Message.class).get();
        Query luceneQuery = builder.bool().must(toTerm(builder, Message.TOKEN, token).createQuery()).createQuery();
        CacheQuery query = searchManager.getQuery(luceneQuery, Message.class);
        return (List<Message>) (List) query.list();
    }
}
