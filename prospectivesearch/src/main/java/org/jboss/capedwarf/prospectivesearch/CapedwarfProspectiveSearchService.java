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

package org.jboss.capedwarf.prospectivesearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityTranslator;
import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.QuerySyntaxException;
import com.google.appengine.api.prospectivesearch.Subscription;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfProspectiveSearchService implements ProspectiveSearchService {

    private final Logger log = Logger.getLogger(getClass().getName());

    private final Cache<TopicAndSubId, SubscriptionHolder> cache;
    private SearchManager searchManager;

    public CapedwarfProspectiveSearchService() {
        ClassLoader classLoader = Application.getAppClassLoader();
        this.cache = createStore().getAdvancedCache().with(classLoader);
        this.searchManager = Search.getSearchManager(cache);
    }

    private Cache<TopicAndSubId, SubscriptionHolder> createStore() {
        return InfinispanUtils.getCache(Application.getAppId(), CacheName.PROSPECTIVE_SEARCH);
    }

    public void subscribe(String topic, String subId, long leaseDurationSeconds, String query, Map<String, FieldType> schema) {
        if (schema.isEmpty()) {
            throw new QuerySyntaxException(subId, topic, query, "schema is empty");
        }

        try {
            Query luceneQuery = parseQuery(query);
            TopicAndSubId key = new TopicAndSubId(topic, subId);
            long expirationTimeSec = System.currentTimeMillis()/1000 + (leaseDurationSeconds == 0 ? 0xffffffffL : leaseDurationSeconds);
            SubscriptionHolder value = new SubscriptionHolder(topic, subId, query, luceneQuery, expirationTimeSec);

            if (leaseDurationSeconds == 0) {
                cache.put(key, value);
            } else {
                cache.put(key, value, leaseDurationSeconds, TimeUnit.SECONDS);
            }

        } catch (ParseException e) {
            throw new QuerySyntaxException(subId, topic, query, e.getMessage());
        }
    }

    private Query parseQuery(String query) throws ParseException {
        return new ProspectiveSearchQueryConverter().convert(query);
    }

    public void unsubscribe(String topic, String subId) {
        SubscriptionHolder holder = cache.remove(new TopicAndSubId(topic, subId));

        if (holder == null) {
            throw new IllegalArgumentException("topic '" + topic + "' has no subscription with subId " + subId);
        }
    }

    private CacheQuery getCacheQuery(Query luceneQuery) {
        return searchManager.getQuery(luceneQuery, SubscriptionHolder.class);
    }

    private QueryBuilder newQueryBuilder() {
        return searchManager.buildQueryBuilderForClass(SubscriptionHolder.class).get();
    }

    public void match(Entity entity, String topic) {
        match(entity, topic, "");
    }

    public void match(Entity entity, String topic, String resultKey) {
        match(entity, topic, resultKey, DEFAULT_RESULT_RELATIVE_URL, DEFAULT_RESULT_TASK_QUEUE_NAME, DEFAULT_RESULT_BATCH_SIZE, true);
    }

    public void match(Entity entity, String topic, String resultKey, String resultRelativeUrl, String resultTaskQueueName, int resultBatchSize, boolean resultReturnDocument) {
        List<Subscription> subscriptions = findMatching(entity, topic);
        addTasks(entity, subscriptions, topic, resultKey, resultRelativeUrl, resultTaskQueueName, resultBatchSize, resultReturnDocument);
    }

    private void addTasks(Entity entity, List<Subscription> subscriptions, String topic, String resultKey, String resultRelativeUrl,
                          String resultTaskQueueName, int resultBatchSize, boolean resultReturnDocument) {
        Queue queue = QueueFactory.getQueue(resultTaskQueueName);

        for (int offset = 0; offset < subscriptions.size(); offset+=resultBatchSize) {
            List<Subscription> batch = subscriptions.subList(offset, Math.min(offset + resultBatchSize, subscriptions.size()));
            TaskOptions taskOptions = TaskOptions.Builder.withUrl(resultRelativeUrl)
                .param("results_offset", String.valueOf(offset))
                .param("results_count", String.valueOf(batch.size()))
                .param("topic", topic)
                .param("key", resultKey);

            for (Subscription subscription : batch) {
                taskOptions.param("id", subscription.getId());
            }

            if (resultReturnDocument) {
                taskOptions.param("document", encodeDocument(entity));
            }

            queue.add(taskOptions);
        }
    }

    private List<Subscription> findMatching(final Entity entity, final String topic) {
        MatchMapper mapper = new MatchMapper(topic, entity);
        MatchReducer reducer = new MatchReducer();
        MatchCollator collator = new MatchCollator();

        MapReduceTask<TopicAndSubId, SubscriptionHolder, String, List<SerializableSubscription>> task = new MapReduceTask<TopicAndSubId, SubscriptionHolder, String, List<SerializableSubscription>>(cache);
        return task.mappedWith(mapper).reducedWith(reducer).execute(collator);
    }

    public List<Subscription> listSubscriptions(String topic) {
        return listSubscriptions(topic, "", DEFAULT_LIST_SUBSCRIPTIONS_MAX_RESULTS, 0);
    }

    public List<Subscription> listSubscriptions(String topic, String subIdStart, int maxResults, long expiresBefore) {
        Query luceneQuery = newQueryBuilder().keyword().onField("topic").matching(topic).createQuery();
        CacheQuery query = getCacheQuery(luceneQuery).maxResults(maxResults);
        List<Object> results = query.list();
        List<Subscription> list = new ArrayList<Subscription>(results.size());
        for (Object o : results) {
            SubscriptionHolder holder = (SubscriptionHolder) o;
            list.add(holder.toSubscription());
        }
        return list;
    }

    public Subscription getSubscription(String topic, String subId) {
        SubscriptionHolder holder = cache.get(new TopicAndSubId(topic, subId));
        if (holder == null) {
            throw new IllegalArgumentException("No subscription with topic '" + topic + "' and subId '" + subId + "'");
        } else {
            return holder.toSubscription();
        }
    }

    public List<String> listTopics(String topicStart, long maxResults) {
        TopicsMapper mapper = new TopicsMapper();
        TopicsReducer reducer = new TopicsReducer();
        TopicsCollator collator = new TopicsCollator();

        MapReduceTask<TopicAndSubId, SubscriptionHolder, String, Set<String>> task = new MapReduceTask<TopicAndSubId, SubscriptionHolder, String, Set<String>>(cache);
        return task.mappedWith(mapper).reducedWith(reducer).execute(collator);
    }

    public Entity getDocument(HttpServletRequest request) {
        return decodeDocument(request.getParameter("document"));
    }

    private String encodeDocument(Entity document) {
        return Base64.encodeWebSafe(EntityTranslator.convertToPb(document).toByteArray(), false);
    }

    private Entity decodeDocument(String encodedDocument) {
        try {
            return EntityTranslator.createFromPbBytes(Base64.decodeWebSafe(encodedDocument));
        } catch (Base64DecoderException e) {
            log.log(Level.WARNING, "Could not decode document: " + encodedDocument, e);
            return null;
        }
    }

    public void clear() {
        cache.clear();
    }
}
