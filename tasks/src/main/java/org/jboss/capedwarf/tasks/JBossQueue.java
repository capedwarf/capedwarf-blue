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

package org.jboss.capedwarf.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.EvictionConfiguration;
import org.infinispan.container.DataContainer;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.jms.ServletExecutorProducer;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;

/**
 * JBoss Queue.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossQueue implements Queue {
    private static final String ID = "ID:";
    private static final Sort SORT = new Sort(new SortField("eta", SortField.LONG));
    private static final TargetInvocation<TaskOptions.Method> getMethod = ReflectionUtils.cacheInvocation(TaskOptions.class, "getMethod");
    private static final TargetInvocation<String> getTaskName = ReflectionUtils.cacheInvocation(TaskOptions.class, "getTaskName");
    private static final TargetInvocation<Long> getEtaMillis = ReflectionUtils.cacheInvocation(TaskOptions.class, "getEtaMillis");
    private static final TargetInvocation<RetryOptions> getRetryOptions = ReflectionUtils.cacheInvocation(TaskOptions.class, "getRetryOptions");

    private final String queueName;
    private final Cache<String, Object> tasks;
    private final SearchManager searchManager;

    public static Queue getQueue(String queueName) {
        return new JBossQueue(queueName); // do not cache
    }

    private JBossQueue(String queueName) {
        validateQueueName(queueName);
        this.queueName = queueName;
        AdvancedCache<String,Object> ac = getCache().getAdvancedCache();
        this.tasks = ac.with(Application.getAppClassloader());
        this.searchManager = Search.getSearchManager(tasks);
    }

    private Cache<String, Object> getCache() {
        Configuration c = InfinispanUtils.getConfiguration(CacheName.TASKS);

        EvictionConfiguration e = c.eviction();
        DataContainer container = new PurgeDataContainer(
                c.locking().concurrencyLevel(),
                e.maxEntries(),
                e.strategy(),
                e.threadPolicy(),
                this);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.read(c);
        builder.dataContainer().dataContainer(container);

        InfinispanUtils.applyIndexing(CacheName.TASKS, builder, TaskOptionsEntity.class, TaskLeaseEntity.class);
        return InfinispanUtils.getCache(CacheName.TASKS, builder.build());
    }

    private Cache<String, Object> getTasks() {
        return tasks;
    }

    protected static MessageCreator createMessageCreator(final TaskOptions taskOptions) {
        return new TasksMessageCreator(taskOptions);
    }

    protected static Transaction getCurrentTransaction() {
        return DatastoreServiceFactory.getDatastoreService().getCurrentTransaction(null);
    }

    protected static String toJmsId(final String name) {
        return ID + name;
    }

    protected static String toTaskName(final String id) {
        return (id.startsWith(ID)) ? id.substring(ID.length()) : id;
    }

    public String getQueueName() {
        return queueName;
    }

    public TaskHandle add() {
        return add(TaskOptions.Builder.withDefaults());
    }

    public TaskHandle add(TaskOptions taskOptions) {
        return add(getCurrentTransaction(), taskOptions);
    }

    public List<TaskHandle> add(Iterable<TaskOptions> taskOptionses) {
        return add(getCurrentTransaction(), taskOptionses);
    }

    public TaskHandle add(final Transaction transaction, final TaskOptions taskOptions) {
        return add(transaction, Collections.singleton(taskOptions)).get(0);
    }

    public List<TaskHandle> add(Transaction transaction, Iterable<TaskOptions> taskOptionses) {
        final ServletExecutorProducer producer = new ServletExecutorProducer();
        try {
            final List<TaskHandle> handles = new ArrayList<TaskHandle>();
            for (TaskOptions to : taskOptionses) {
                TaskOptions copy = null;
                final TaskOptions.Method m = getMethod.invoke(to);
                if (m == TaskOptions.Method.PULL) {
                    copy = new TaskOptions(to);
                    String taskName = getTaskName.invoke(to);
                    if (taskName == null) {
                        taskName = UUID.randomUUID().toString(); // TODO -- unique enough?
                        copy.taskName(taskName);
                    }
                    Long lifespan = getEtaMillis.invoke(to);
                    RetryOptions retryOptions = getRetryOptions.invoke(to);
                    getTasks().put(taskName, new TaskOptionsEntity(taskName, queueName, copy.getTag(), lifespan, copy, retryOptions), lifespan, TimeUnit.MILLISECONDS);
                } else if (m == TaskOptions.Method.POST) {
                    final MessageCreator mc = createMessageCreator(to);
                    final String id = producer.sendMessage(mc);
                    copy = new TaskOptions(to);
                    if (getTaskName.invoke(to) == null)
                        copy.taskName(toTaskName(id));
                }
                if (copy != null)
                    handles.add(new TaskHandle(copy, getQueueName()));
            }
            return handles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            producer.dispose();
        }
    }

    public boolean deleteTask(String taskName) {
        validateTaskName(taskName);
        final Cache<String, Object> cache = getTasks();
        return (cache.remove(TaskLeaseEntity.LEASE + taskName) != null) || (cache.remove(taskName) != null);
    }

    public boolean deleteTask(TaskHandle taskHandle) {
        return deleteTask(taskHandle.getName());
    }

    public List<Boolean> deleteTask(List<TaskHandle> taskHandles) {
        List<Boolean> results = new ArrayList<Boolean>();
        for (TaskHandle th : taskHandles)
            results.add(deleteTask(th));
        return results;
    }

    public List<TaskHandle> leaseTasks(long lease, TimeUnit unit, long countLimit) {
        return leaseTasksByTag(lease, unit, countLimit, null);
    }

    public List<TaskHandle> leaseTasksByTagBytes(long lease, TimeUnit unit, long countLimit, byte[] tag) {
        return leaseTasksByTag(lease, unit, countLimit, tag == null ? null : new String(tag));
    }

    public List<TaskHandle> leaseTasksByTag(long lease, TimeUnit unit, long countLimit, String tag) {
        return leaseTasks(new LeaseOptionsInternal(lease, unit, countLimit, tag));
    }

    public List<TaskHandle> leaseTasks(LeaseOptions options) {
        return leaseTasks(new LeaseOptionsInternal(options));
    }

    @SuppressWarnings("unchecked")
    protected List<TaskHandle> leaseTasks(LeaseOptionsInternal options) {
        final QueryBuilder builder = searchManager.buildQueryBuilderForClass(TaskOptionsEntity.class).get();
        final Query queueQuery = builder.keyword().onField("queue").matching(queueName).createQuery();
        final String tag = options.getTag();
        final Query lq;
        if (tag == null) {
            if (options.isGroupByTag()) {
                final CacheQuery first = searchManager.getQuery(queueQuery, TaskOptionsEntity.class).maxResults(1).sort(SORT);
                final List<Object> singleton = first.list();
                if (singleton.isEmpty()) {
                    return Collections.emptyList();
                } else {
                    final String tmp = TaskOptionsEntity.class.cast(singleton.get(0)).getTag();
                    final Query tagQuery = builder.keyword().onField("tag").matching(tmp).createQuery();
                    lq = builder.bool().must(queueQuery).must(tagQuery).createQuery();
                }
            } else {
                lq = queueQuery;
            }
        } else {
            final Query tagQuery = builder.keyword().onField("tag").matching(tag).createQuery();
            lq = builder.bool().must(queueQuery).must(tagQuery).createQuery();
        }

        final CacheQuery query = searchManager.getQuery(lq, TaskOptionsEntity.class)
                .maxResults((int) options.getCountLimit())
                .sort(SORT);

        final List<TaskHandle> handles = new ArrayList<TaskHandle>();
        for (Object obj : query) {
            TaskOptionsEntity toe = (TaskOptionsEntity) obj;
            final String name = toe.getName();
            final Cache<String, Object> cache = getTasks();
            cache.remove(name);
            cache.put(TaskLeaseEntity.LEASE + name, new TaskLeaseEntity(toe.getOptions()), options.getLease(), options.getUnit());
            handles.add(new TaskHandle(toe.getOptions(), queueName));
        }
        return handles;
    }

    public void purge() {
        getTasks().clear();
    }

    public TaskHandle modifyTaskLease(TaskHandle taskHandle, long lease, TimeUnit unit) {
        final String name = taskHandle.getName();
        final Cache<String, Object> cache = getTasks();
        TaskLeaseEntity tle = (TaskLeaseEntity) cache.get(TaskLeaseEntity.LEASE + name);
        if (tle != null) {
            if (lease == 0) {
                cache.remove(TaskLeaseEntity.LEASE + name);
                return add(tle.getOptions());
            } else {
                cache.replace(TaskLeaseEntity.LEASE + name, tle, lease, unit);
                taskHandle = new TaskHandle(tle.getOptions().etaMillis(unit.toMillis(lease)), queueName);
            }
        }
        return taskHandle;
    }

    static void validateQueueName(String queueName) {
        if (queueName == null || queueName.length() == 0 || QueueConstants.QUEUE_NAME_PATTERN.matcher(queueName).matches() == false) {
            throw new IllegalArgumentException("Queue name does not match expression " + QueueConstants.QUEUE_NAME_REGEX + "; found '" + queueName + "'");
        }
    }

    static void validateTaskName(String taskName) {
        if (taskName == null || taskName.length() == 0 || QueueConstants.TASK_NAME_PATTERN.matcher(taskName).matches() == false) {
            throw new IllegalArgumentException("Task name does not match expression " + QueueConstants.TASK_NAME_REGEX + "; given taskname: '" + taskName + "'");
        }
    }
}
