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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.jms.ServletExecutorProducer;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JBoss Queue.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossQueue implements Queue {
    private static final String ID = "ID:";
    private static final Map<String, Queue> cache = new HashMap<String, Queue>();
    private static final TargetInvocation<TaskOptions.Method> getMethod = ReflectionUtils.cacheInvocation(TaskOptions.class, "getMethod");
    
    private final String queueName;

    public static synchronized Queue getQueue(String queueName) {
        Queue queue = cache.get(queueName);
        if (queue == null) {
            queue = new JBossQueue(queueName);
            cache.put(queueName, queue);
        }
        return queue;
    }

    private JBossQueue(String queueName) {
        validateQueueName(queueName);
        this.queueName = queueName;
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
                final TaskOptions.Method m = getMethod.invoke(to);
                if (m == TaskOptions.Method.PULL)
                    throw new IllegalArgumentException("PULL method is not yet supported: " + to);

                final MessageCreator mc = createMessageCreator(to);
                final String id = producer.sendMessage(mc);
                final TaskOptions copy = new TaskOptions(to).taskName(toTaskName(id));
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
        return deleteTask(new TaskHandle(TaskOptions.Builder.withTaskName(taskName), queueName));
    }

    public boolean deleteTask(TaskHandle taskHandle) {
        return false; // TODO
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
        return leaseTasksByTag(lease, unit, countLimit, new String(tag));
    }

    public List<TaskHandle> leaseTasksByTag(long lease, TimeUnit unit, long countLimit, String tag) {
        return null;  // TODO
    }

    public void purge() {
        // TODO
    }

    public TaskHandle modifyTaskLease(TaskHandle taskHandle, long lease, TimeUnit unit) {
        return null;  // TODO
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
