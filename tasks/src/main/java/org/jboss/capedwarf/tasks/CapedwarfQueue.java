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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.InvalidQueueModeException;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.QueueStatistics;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermTermination;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.jms.ServletExecutorProducer;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.shared.config.QueueXml;

/**
 * JBoss Queue.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@QueueInitialization
class CapedwarfQueue implements Queue {
    private static final String ID = "ID:";
    private static final Sort SORT = new Sort(new SortField("eta", SortField.LONG));

    private final String queueName;

    private volatile boolean initilized;

    private boolean isPushQueue;
    private Cache<String, Object> tasks;
    private SearchManager searchManager;
    private DatastoreService datastoreService;

    public static Queue getQueue(String queueName) {
        return new CapedwarfQueue(queueName); // do not cache
    }

    private CapedwarfQueue(String queueName) {
        validateQueueName(queueName);
        this.queueName = queueName;
    }

    void initialize() {
        if (initilized == false) {
            synchronized (this) {
                if (initilized == false) {
                    CapedwarfEnvironment env = CapedwarfEnvironment.getThreadLocalInstance();
                    QueueXml qx = env.getQueueXml();
                    QueueXml.Queue queue = qx.getQueues().get(queueName);
                    if (queue == null) {
                        throw new IllegalStateException("No such queue " + queueName + " in queue.xml!");
                    }

                    this.isPushQueue = queue.getMode() == QueueXml.Mode.PUSH;
                    AdvancedCache<String, Object> ac = getCache().getAdvancedCache();
                    this.tasks = ac.with(Application.getAppClassloader());
                    this.searchManager = Search.getSearchManager(tasks);

                    this.datastoreService = DatastoreServiceFactory.getDatastoreService();

                    initilized = true;
                }
            }
        }
    }

    private Cache<String, Object> getCache() {
        return InfinispanUtils.getCache(Application.getAppId(), CacheName.TASKS);
    }

    private Cache<String, Object> getTasks() {
        return tasks;
    }

    protected MessageCreator createMessageCreator(final TaskOptions taskOptions) {
        return new TasksMessageCreator(queueName, taskOptions);
    }

    protected Transaction getCurrentTransaction() {
        return datastoreService.getCurrentTransaction(null);
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

    public List<TaskHandle> add(Transaction transaction, Iterable<TaskOptions> taskOptions) {
        assertTaskOptionsMatchQueueType(taskOptions);
        ServletExecutorProducer producer = new ServletExecutorProducer();
        try {
            List<TaskHandle> handles = new ArrayList<TaskHandle>();
            for (TaskOptions to : taskOptions) {
                TaskHandle handle = addTask(producer, to);
                handles.add(handle);
            }
            return handles;
        } finally {
            producer.dispose();
        }
    }

    private void assertTaskOptionsMatchQueueType(Iterable<TaskOptions> taskOptions) {
        for (TaskOptions options : taskOptions) {
            TaskOptionsHelper helper = new TaskOptionsHelper(options);
            TaskOptions.Method method = helper.getMethod();
            if (isPushQueue ? method == TaskOptions.Method.PULL : method != TaskOptions.Method.PULL) {
                throw new InvalidQueueModeException("Target queue mode does not support this operation");
            }
            if (isPushQueue && helper.getTagAsBytes() != null) {
                throw new IllegalArgumentException("Only PULL tasks can have a tag.");
            }
        }
    }

    private TaskHandle addTask(ServletExecutorProducer producer, TaskOptions to) {
        try {
            TaskOptionsHelper helper = new TaskOptionsHelper(to);
            TaskOptions copy = new TaskOptions(to);
            if (helper.getMethod() == TaskOptions.Method.PULL) {
                String taskName = helper.getTaskName();
                if (taskName == null) {
                    taskName = UUID.randomUUID().toString(); // TODO -- unique enough?
                    copy.taskName(taskName);
                }
                Long lifespan = helper.getEtaMillis();
                RetryOptions retryOptions = helper.getRetryOptions();
                TaskOptionsEntity taskOptionsEntity = new TaskOptionsEntity(taskName, queueName, copy.getTag(), lifespan, copy, retryOptions);
                getTasks().put(taskName, taskOptionsEntity, lifespan == null ? -1 : lifespan, TimeUnit.MILLISECONDS);
            } else {
                MessageCreator mc = createMessageCreator(to);
                String id = producer.sendMessage(mc);
                if (helper.getTaskName() == null)
                    copy.taskName(toTaskName(id));
            }
            return new TaskHandle(copy, getQueueName());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        assertPullQueue();
        final QueryBuilder builder = searchManager.buildQueryBuilderForClass(TaskOptionsEntity.class).get();
        final Query queueQuery = toTerm(builder, "queue", queueName).createQuery();
        final String tag = options.getTagAsString();
        final Query lq;
        if (tag == null) {
            if (options.isGroupByTag()) {
                final CacheQuery first = searchManager.getQuery(queueQuery, TaskOptionsEntity.class).maxResults(1).sort(SORT);
                final List<Object> singleton = first.list();
                if (singleton.isEmpty()) {
                    return Collections.emptyList();
                } else {
                    final String tmp = TaskOptionsEntity.class.cast(singleton.get(0)).getTag();
                    final Query tagQuery = toTerm(builder, "tag", tmp).createQuery();
                    lq = builder.bool().must(queueQuery).must(tagQuery).createQuery();
                }
            } else {
                lq = queueQuery;
            }
        } else {
            final Query tagQuery = toTerm(builder, "tag", tag).createQuery();
            lq = builder.bool().must(queueQuery).must(tagQuery).createQuery();
        }

        final CacheQuery query = searchManager.getQuery(lq, TaskOptionsEntity.class)
                .maxResults((int) options.getCountLimit())
                .sort(SORT);

        final List<TaskHandle> handles = new ArrayList<TaskHandle>();
        for (Object obj : query.list()) {   // must not use query.iterator() because of ISPN-2852
            TaskOptionsEntity toe = (TaskOptionsEntity) obj;
            final String name = toe.getName();
            final Cache<String, Object> cache = getTasks();
            cache.remove(name);
            cache.put(TaskLeaseEntity.LEASE + name, new TaskLeaseEntity(getQueueName(), toe.getOptions()), options.getLease(), options.getUnit());
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

    private void assertPullQueue() {
        if (isPushQueue) {
            throw new InvalidQueueModeException("Target queue mode does not support this operation");
        }
    }

    protected QueueStatisticsInternal createQueueStatistics() {
        return new QueueStatisticsImpl(queueName, searchManager);
    }

    public QueueStatistics fetchStatistics() {
        return createQueueStatistics().fetchStatistics();
    }

    public QueueStatistics fetchStatistics(double v) {
        // TODO -- check what is v?
        return createQueueStatistics().fetchStatistics(v);
    }

    public Future<TaskHandle> addAsync() {
        return ExecutorFactory.wrap(new Callable<TaskHandle>() {
            public TaskHandle call() throws Exception {
                return add();
            }
        });
    }

    public Future<TaskHandle> addAsync(final TaskOptions taskOptions) {
        return ExecutorFactory.wrap(new Callable<TaskHandle>() {
            public TaskHandle call() throws Exception {
                return add(taskOptions);
            }
        });
    }

    public Future<List<TaskHandle>> addAsync(final Iterable<TaskOptions> taskOptionses) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return add(taskOptionses);
            }
        });
    }

    public Future<TaskHandle> addAsync(final Transaction transaction, final TaskOptions taskOptions) {
        return ExecutorFactory.wrap(new Callable<TaskHandle>() {
            public TaskHandle call() throws Exception {
                return add(transaction, taskOptions);
            }
        });
    }

    public Future<List<TaskHandle>> addAsync(final Transaction transaction, final Iterable<TaskOptions> taskOptionses) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return add(transaction, taskOptionses);
            }
        });
    }

    public Future<Boolean> deleteTaskAsync(final String taskName) {
        return ExecutorFactory.wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return deleteTask(taskName);
            }
        });
    }

    public Future<Boolean> deleteTaskAsync(final TaskHandle taskHandle) {
        return ExecutorFactory.wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return deleteTask(taskHandle);
            }
        });
    }

    public Future<List<Boolean>> deleteTaskAsync(final List<TaskHandle> taskHandles) {
        return ExecutorFactory.wrap(new Callable<List<Boolean>>() {
            public List<Boolean> call() throws Exception {
                return deleteTask(taskHandles);
            }
        });
    }

    public Future<List<TaskHandle>> leaseTasksAsync(final long lease, final TimeUnit unit, final long countLimit) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return leaseTasks(lease, unit, countLimit);
            }
        });
    }

    public Future<List<TaskHandle>> leaseTasksByTagBytesAsync(final long lease, final TimeUnit unit, final long countLimit, final byte[] tag) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return leaseTasksByTagBytes(lease, unit, countLimit, tag);
            }
        });
    }

    public Future<List<TaskHandle>> leaseTasksByTagAsync(final long lease, final TimeUnit unit, final long countLimit, final String tag) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return leaseTasksByTag(lease, unit, countLimit, tag);
            }
        });
    }

    public Future<List<TaskHandle>> leaseTasksAsync(final LeaseOptions leaseOptions) {
        return ExecutorFactory.wrap(new Callable<List<TaskHandle>>() {
            public List<TaskHandle> call() throws Exception {
                return leaseTasks(leaseOptions);
            }
        });
    }

    public Future<QueueStatistics> fetchStatisticsAsync() {
        return ExecutorFactory.wrap(new Callable<QueueStatistics>() {
            public QueueStatistics call() throws Exception {
                return fetchStatistics();
            }
        });
    }

    public Future<QueueStatistics> fetchStatisticsAsync(final double v) {
        return ExecutorFactory.wrap(new Callable<QueueStatistics>() {
            public QueueStatistics call() throws Exception {
                return fetchStatistics(v);
            }
        });
    }

    static TermTermination toTerm(QueryBuilder builder, String field, Object value) {
        return builder.keyword().onField(field).ignoreAnalyzer().ignoreFieldBridge().matching(value);
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
