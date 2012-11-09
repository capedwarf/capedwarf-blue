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

import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ExecutorFactory {
    private static String[] defaultJndiNames = {"java:jboss/threads/executor/capedwarf"};
    private static volatile Executor executor;

    private static int refCount = 0;

    /**
     * Get Executor instance.
     * First lookup JNDI, then use default if none found.
     *
     * @return the Executor instance
     */
    public static Executor getInstance() {
        if (executor == null) {
            synchronized (ExecutorFactory.class) {
                if (executor == null) {
                    executor = doJndiLookup();
                }
            }
        }
        return executor;
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

    protected static Executor doJndiLookup() {
        try {
            return JndiLookupUtils.lookup("jndi.executor", Executor.class, defaultJndiNames);
        } catch (Throwable t) {
            Logger.getLogger(Executor.class.getName()).fine("No Executor found in JNDI: " + t.getMessage());
            return null;
        }
    }

    public static void registerApp(String appId) {
        synchronized (ExecutorFactory.class) {
            refCount++;
        }
    }

    public static void unregisterApp(String appId) {
        synchronized (ExecutorFactory.class) {
            refCount--;
            if (refCount == 0) {
                executor = null;
            }
        }
    }
}
