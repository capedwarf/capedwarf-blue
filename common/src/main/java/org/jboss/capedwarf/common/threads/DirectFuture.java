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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Very simple direct future.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DirectFuture<T> implements Future<T> {
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Callable<T> callable;
    private T result;
    private boolean canceled;

    private DirectFuture(Callable<T> callable) {
        // should not return null
        this.callable = callable;
    }

    public static <C> Future<C> create(Callable<C> callable) {
        return new DirectFuture<C>(callable);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        lock.writeLock().lock();
        try {
            if (result == null) {
                canceled = true;
                return true;
            } else {
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isCancelled() {
        lock.readLock().lock();
        try {
            return canceled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isDone() {
        lock.readLock().lock();
        try {
            return (result != null);
        } finally {
            lock.readLock().unlock();
        }
    }

    public T get() throws InterruptedException, ExecutionException {
        lock.writeLock().lock();
        try {
            return getInternal();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw RuntimeException.class.cast(e);
            } else if (e instanceof ExecutionException) {
                throw ExecutionException.class.cast(e);
            } else if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw InterruptedException.class.cast(e);
            } else {
                throw new ExecutionException(e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final long now = System.currentTimeMillis();
        if (lock.writeLock().tryLock(timeout, unit) == false) {
            throw new TimeoutException("Cannot get a lock in " + unit.toMillis(timeout) + "ms!");
        }
        try {
            final T result = getInternal();

            if (System.currentTimeMillis() - now > unit.toMicros(timeout))
                throw new TimeoutException("get() took too much time!");

            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw RuntimeException.class.cast(e);
            } else if (e instanceof TimeoutException) {
                throw TimeoutException.class.cast(e);
            } else if (e instanceof ExecutionException) {
                throw ExecutionException.class.cast(e);
            } else if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw InterruptedException.class.cast(e);
            } else {
                throw new ExecutionException(e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Should be invoked with a lock.
     */
    protected T getInternal() throws Exception {
        if (canceled)
            throw new CancellationException("Already canceled: " + callable);

        if (result == null) {
            result = callable.call();
        }
        return result;
    }
}
