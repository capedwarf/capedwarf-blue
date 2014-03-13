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

package org.jboss.capedwarf.common.async;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.SimpleAppIdFactory;

/**
 * Distributable callable.
 * Can be shared accross the wire.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class DistributableWrapper<V> implements DistributedCallable<Object, Object, V>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String appId;
    private final String module;
    private final CapedwarfEnvironment env;
    private final Callable<V> callable;

    DistributableWrapper(Callable<V> callable) {
        this.appId = AppIdFactory.getAppId();
        this.module = AppIdFactory.getModule();
        this.env = CapedwarfEnvironment.getThreadLocalInstance().clone();
        this.callable = callable;
    }

    public V call() throws Exception {
        AppIdFactory.setCurrentFactory(new SimpleAppIdFactory(appId, module));
        try {
            final CapedwarfEnvironment previous = CapedwarfEnvironment.setThreadLocalInstance(env);
            try {
                return callable.call();
            } finally {
                CapedwarfEnvironment.setThreadLocalInstance(previous);
            }
        } finally {
            AppIdFactory.resetCurrentFactory();
        }
    }

    @SuppressWarnings("unchecked")
    public void setEnvironment(Cache<Object, Object> cache, Set<Object> inputKeys) {
        if (callable instanceof DistributedCallable) {
            DistributedCallable.class.cast(callable).setEnvironment(cache, inputKeys);
        }
    }
}
