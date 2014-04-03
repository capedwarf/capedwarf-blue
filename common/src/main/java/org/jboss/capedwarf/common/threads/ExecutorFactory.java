/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.infinispan.util.concurrent.WithinThreadExecutor;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ExecutorFactory {
    private static volatile ExecutorService direct = new WithinThreadExecutor();

    /**
     * Get Executor instance.
     * First lookup JNDI, then use default if none found.
     *
     * @return the Executor instance
     */
    public static ExecutorService getInstance() {
        return ComponentRegistry.getInstance().getComponent(Keys.EXECUTOR_SERVICE);
    }

    /**
     * Get direct executor.
     *
     * @return direct executor
     */
    public static ExecutorService getDirectExecutor() {
        return direct; // TODO - more configurable
    }

    /**
     * Wrap callable into future task.
     *
     * @param callable the callable
     * @return future task
     */
    public static <T> Future<T> wrap(Callable<T> callable) {
        final FutureTask<T> task = new FutureTask<T>(callable);
        final Executor executor = getInstance();
        executor.execute(task);
        return task;
    }
}
