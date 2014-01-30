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
import java.util.concurrent.Future;

import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.SimpleAppIdFactory;
import org.jboss.capedwarf.shared.util.Utils;

/**
 * Abstract cache listener
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractCacheListener {
    private final CapedwarfEnvironment env = CapedwarfEnvironment.getThreadLocalInstance();

    /**
     * Execute callable on distributed framework.
     * Make sure callable is fully initialized.
     *
     * @param callable the callable
     * @param key the task key
     */
    protected <T> void executeCallable(final Callable<T> callable, final Object key) {
        executeCallable(callable, key, true);
    }

    /**
     * Execute callable on distributed framework.
     * Make sure callable is fully initialized.
     *
     * @param callable the callable
     * @param key the task key
     * @param block the block flag
     */
    protected <T> void executeCallable(final Callable<T> callable, final Object key, final boolean block) {
        executeCallable(new Taskable<T>() {
            public Callable<T> toCallable() {
                return callable;
            }

            public Object taskKey() {
                return key;
            }

            public boolean block() {
                return block;
            }
        });
    }

    /**
     * Execute taskable.
     *
     * @param taskable the taskable
     */
    protected <T> void executeCallable(Taskable<T> taskable) {
        final String appId = env.getAppId();
        final String module = env.getModuleId();
        AppIdFactory.setCurrentFactory(new SimpleAppIdFactory(appId, module));
        try {
            final CapedwarfEnvironment previous = CapedwarfEnvironment.setThreadLocalInstance(env);
            try {
                final Future<T> future = InfinispanUtils.fire(appId, CacheName.DIST, taskable.toCallable(), taskable.taskKey());
                if (taskable.block()) {
                    Utils.quietGet(future);
                }
            } finally {
                CapedwarfEnvironment.setThreadLocalInstance(previous);
            }
        } finally {
            AppIdFactory.resetCurrentFactory();
        }
    }
}
