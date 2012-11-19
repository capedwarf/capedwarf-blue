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

package org.jboss.capedwarf.datastore.notifications;

import java.util.concurrent.Callable;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.config.JBossEnvironment;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * Abstract cache listener
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractCacheListener {
    private final JBossEnvironment env = JBossEnvironment.getThreadLocalInstance();

    /**
     * Execute callable on distributed framework.
     * Make sure callable is fully initialized.
     *
     * @param callable the callable
     * @param key the task key
     */
    protected <T> void executeCallable(final Callable<T> callable, final Object key) {
        executeCallable(new Taskable<T>() {
            public Callable<T> toCallable() {
                return callable;
            }

            public Object taskKey() {
                return key;
            }
        });
    }

    /**
     * Execute taskable.
     *
     * @param taskable the taskable
     */
    protected <T> void executeCallable(Taskable<T> taskable) {
        JBossEnvironment previous = JBossEnvironment.setThreadLocalInstance(env);
        try {
            InfinispanUtils.submit(Application.getAppId(), CacheName.DIST, taskable.toCallable(), taskable.taskKey());
        } finally {
            if (previous != null) {
                JBossEnvironment.setThreadLocalInstance(previous);
            } else {
                JBossEnvironment.clearThreadLocalInstance();
            }
        }
    }
}
