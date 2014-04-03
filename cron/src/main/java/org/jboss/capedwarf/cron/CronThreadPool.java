/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.cron;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;
import org.jboss.capedwarf.shared.util.Utils;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CronThreadPool implements ThreadPool {
    private ModuleLoader loader;
    private ModuleIdentifier moduleIdentifier;

    public boolean runInThread(Runnable runnable) {
        try {
            Runnable wrapper = new TcclWrapper(runnable);
            ExecutorFactory.getInstance().execute(wrapper);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    public int blockForAvailableThreads() {
        ExecutorService executor = ExecutorFactory.getInstance();
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = ThreadPoolExecutor.class.cast(executor);
            return (tpe.getMaximumPoolSize() - tpe.getActiveCount());
        } else {
            return 1; // there should always be some thread?
        }
    }

    public void initialize() throws SchedulerConfigException {
        loader = ComponentRegistry.getInstance().getComponent(Keys.MODULE_LOADER);
    }

    public void shutdown(boolean waitForJobsToComplete) {
    }

    public int getPoolSize() {
        ExecutorService executor = ExecutorFactory.getInstance();
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = ThreadPoolExecutor.class.cast(executor);
            return tpe.getMaximumPoolSize();
        } else {
            return 1; // there should always be some thread?
        }
    }

    public void setInstanceId(String schedInstId) {
    }

    public void setInstanceName(String schedName) {
    }

    public void setModule(String module) {
        this.moduleIdentifier = ModuleIdentifier.fromString(module);
    }

    private class TcclWrapper implements Runnable {
        private Runnable delegate;

        private TcclWrapper(Runnable delegate) {
            this.delegate = delegate;
        }

        public void run() {
            final ClassLoader cl;
            try {
                Module module = loader.loadModule(moduleIdentifier);
                cl = module.getClassLoader();
            } catch (ModuleLoadException e) {
                throw Utils.toRuntimeException(e);
            }
            final ClassLoader previous = Utils.setTCCL(cl);
            try {
                delegate.run();
            } finally {
                Utils.setTCCL(previous);
            }
        }
    }
}
