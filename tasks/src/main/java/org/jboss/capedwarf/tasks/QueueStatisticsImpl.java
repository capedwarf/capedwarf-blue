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

import java.util.Enumeration;

import javax.jms.QueueBrowser;

import com.google.appengine.api.taskqueue.QueueStatistics;
import com.google.appengine.api.taskqueue.TaskQueuePb;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.query.SearchManager;
import org.jboss.capedwarf.common.jms.JmsAdapter;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * Default Queue stats impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class QueueStatisticsImpl extends JmsAdapter implements QueueStatisticsInternal {
    static final Class[] types = new Class[]{String.class, TaskQueuePb.TaskQueueFetchQueueStatsResponse.QueueStats.class};

    private final SearchManager manager;

    QueueStatisticsImpl(SearchManager manager) {
        this.manager = manager;
    }

    public QueueStatistics fetchStatistics(String queueName) {
        try {
            return fetchStatisticsInternal(queueName);
        } finally {
            dispose();
        }
    }

    protected QueueStatistics fetchStatisticsInternal(String queueName) {
        try {
            TaskQueuePb.TaskQueueFetchQueueStatsResponse.QueueStats stats = new TaskQueuePb.TaskQueueFetchQueueStatsResponse.QueueStats();

            int numTasks;
            long oldestEtaUsec = -1L;
            int requestsInFlight = 0;
            double enforcedRate = 0;

            final QueueBrowser browser = getBrowser();
            try {
                Enumeration enumeration = browser.getEnumeration();
                while (enumeration.hasMoreElements()) {
                    requestsInFlight++;
                    enumeration.nextElement();
                }
            } finally {
                browser.close();
            }

            QueryBuilder builder = manager.buildQueryBuilderForClass(TaskOptionsEntity.class).get();
            Query query = CapedwarfQueue.toTerm(builder, "queue", queueName).createQuery();
            int pullTasksSize = manager.getQuery(query, TaskOptionsEntity.class).getResultSize();

            numTasks = requestsInFlight + pullTasksSize;

            stats.setNumTasks(numTasks);
            stats.setOldestEtaUsec(oldestEtaUsec);

            TaskQueuePb.TaskQueueScannerQueueInfo sqi = new TaskQueuePb.TaskQueueScannerQueueInfo();
            sqi.setRequestsInFlight(requestsInFlight);
            sqi.setEnforcedRate(enforcedRate);
            stats.setScannerInfo(sqi);

            return ReflectionUtils.newInstance(QueueStatistics.class, types, new Object[]{queueName, stats});
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
