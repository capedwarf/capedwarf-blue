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

package org.jboss.capedwarf.common.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DirectFuture<T> implements Future<T> {
    private final Callable<T> callable;
    private volatile T result;
    private volatile boolean canceled;

    private DirectFuture(Callable<T> callable) {
        // should not return null
        this.callable = callable;
    }

    public static <C> Future<C> create(Callable<C> callable) {
        return new DirectFuture<C>(callable);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (result == null) {
            canceled = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean isCancelled() {
        return canceled;
    }

    public boolean isDone() {
        return (result != null);
    }

    public T get() throws InterruptedException, ExecutionException {
        try {
            if (canceled == false && result == null) {
                result = callable.call();
            }
            return result;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long now = System.currentTimeMillis();
        T tmp = get();
        if (System.currentTimeMillis() - now > unit.toMillis(timeout)) {
            throw new TimeoutException("get() took too much time.");
        }
        return tmp;
    }
}
