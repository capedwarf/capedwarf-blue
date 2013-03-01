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

package org.jboss.capedwarf.common.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.common.util.Util;

/**
 * Wrappers - env, CL, ApiProxy, ...
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class Wrappers {
    /**
     * Wrap callable.
     *
     * @param callable the callable
     * @return wrapped callable in future
     */
    public static <T> Callable<T> wrap(final Callable<T> callable) {
        return new CallableWrapper<T>(callable);
    }

    /**
     * Wrap callable to Future.
     *
     * @param callable the callable
     * @return wrapped callable in future
     */
    public static <T> Future<T> future(Callable<T> callable) {
        return ExecutorFactory.wrap(wrap(callable));
    }

    /**
     * Wrap runnable.
     *
     * @param runnable the runnable
     * @return wrapped runnable
     */
    public static Runnable wrap(Runnable runnable) {
        return new RunnableWrapper(runnable);
    }

    private static class CallableWrapper<V> implements Callable<V> {
        private final ClassLoader appCL;
        private final CapedwarfEnvironment env;
        private final Callable<V> callable;

        private CallableWrapper(Callable<V> callable) {
            this.appCL = Application.getAppClassloader();
            this.env = CapedwarfEnvironment.getThreadLocalInstance();
            this.callable = callable;
        }

        public V call() throws Exception {
            final ClassLoader old = SecurityActions.setThreadContextClassLoader(appCL);
            try {
                CapedwarfEnvironment.setThreadLocalInstance(env);
                try {
                    return callable.call();
                } finally {
                    CapedwarfEnvironment.clearThreadLocalInstance();
                }
            } finally {
                SecurityActions.setThreadContextClassLoader(old);
            }
        }
    }

    private static class RunnableWrapper implements Runnable {
        private final Callable<Void> callable;

        private RunnableWrapper(final Runnable runnable) {
            this.callable = new CallableWrapper<Void>(new Callable<Void>() {
                public Void call() throws Exception {
                    runnable.run();
                    return null;
                }
            });
        }

        public void run() {
            try {
                callable.call();
            } catch (Exception e) {
                throw Util.toRuntimeException(e);
            }
        }
    }
}
