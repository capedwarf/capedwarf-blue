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

package org.jboss.capedwarf.common.infinispan;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

import org.infinispan.Cache;
import org.infinispan.distexec.DistributedCallable;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;
import org.jboss.capedwarf.shared.util.Utils;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * TCCL wrapper.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TCCLCallable<R> implements DistributedCallable<Object, Object, R>, Serializable {
    private final Callable<R> delegate;
    private ModuleIdentifier moduleIdentifier;

    public TCCLCallable(Callable<R> delegate) {
        this.delegate = delegate;
        this.moduleIdentifier = Utils.toModule().getIdentifier();
    }

    public R call() throws Exception {
        final ModuleLoader ml = ComponentRegistry.getInstance().getComponent(Keys.MODULE_LOADER);
        final Module module = ml.loadModule(moduleIdentifier);

        final ClassLoader previous = Utils.getTCCL();
        Utils.setTCCL(module.getClassLoader());
        try {
            return delegate.call();
        } finally {
            Utils.setTCCL(previous);
        }
    }

    @SuppressWarnings("unchecked")
    public void setEnvironment(Cache<Object, Object> cache, Set<Object> inputKeys) {
        if (delegate instanceof DistributedCallable) {
            DistributedCallable.class.cast(delegate).setEnvironment(cache, inputKeys);
        }
    }
}
