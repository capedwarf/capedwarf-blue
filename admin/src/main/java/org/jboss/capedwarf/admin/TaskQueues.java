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

package org.jboss.capedwarf.admin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.QueueStatistics;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.QueueXml;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Named
@RequestScoped
public class TaskQueues {

    private List<TaskQueue> pushQueues;
    private List<TaskQueue> pullQueues;

    public List<TaskQueue> getPushQueues() {
        if (pushQueues == null) {
            loadQueues();
        }
        return pushQueues;
    }

    public List<TaskQueue> getPullQueues() {
        if (pullQueues == null) {
            loadQueues();
        }
        return pullQueues;
    }

    @PostConstruct
    private void loadQueues() {
        pushQueues = new ArrayList<TaskQueue>();
        pullQueues = new ArrayList<TaskQueue>();

        for (QueueXml.Queue queue : ApplicationConfiguration.getInstance().getQueueXml().getQueues().values()) {
            String name = queue.getName();
            if (QueueXml.INTERNAL.equals(name) == false) {
                TaskQueue taskQueue = new TaskQueue(QueueFactory.getQueue(name));
                if (queue.getMode() == QueueXml.Mode.PUSH) {
                    pushQueues.add(taskQueue);
                } else if (queue.getMode() == QueueXml.Mode.PULL) {
                    pullQueues.add(taskQueue);
                }
            }
        }
    }

    public class TaskQueue {

        private Queue queue;
        private QueueStatistics statistics;

        public TaskQueue(Queue queue) {
            this.queue = queue;
            statistics = queue.fetchStatistics();
        }

        public String getName() {
            return queue.getQueueName();
        }

        public String getMaximumRate() {
            return statistics.getEnforcedRate() + "/s";
        }

        public String getBucketSize() {
            return "";   // TODO
        }

        public String getOldestTask() {
            return statistics.getOldestEtaUsec() == null ? "" : String.valueOf(statistics.getOldestEtaUsec());
        }

        public int getTasksInQueue() {
            return statistics.getNumTasks();
        }

    }
}
