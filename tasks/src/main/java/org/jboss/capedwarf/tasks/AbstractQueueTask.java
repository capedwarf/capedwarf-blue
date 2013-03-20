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

package org.jboss.capedwarf.tasks;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.util.Util;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractQueueTask implements Callable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String queueName;
    private final String taskName;

    public AbstractQueueTask(String queueName) {
        this(queueName, null);
    }

    public AbstractQueueTask(String queueName, String taskName) {
        this.queueName = queueName;
        this.taskName = taskName;
    }

    public Long call() throws Exception {
        long count = 0;
        count += QueueUtils.count(queueName, taskName);
        count += QueueUtils.scheduled(queueName, taskName).size();
        return count;
    }

    static long count(Callable<Long> task) {
        List<Future<Long>> results = InfinispanUtils.everywhere(Application.getAppId(), task);
        long count = 0;
        for (Future<Long> f : results) {
            count += Util.quietGet(f);
        }
        return count;
    }
}
