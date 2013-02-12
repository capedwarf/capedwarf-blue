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

package org.jboss.capedwarf.common.config;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.ThreadFactory;

import org.jboss.capedwarf.common.async.Wrappers;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyThreadFactory implements ThreadFactory, Serializable {
    private static final long serialVersionUID = 1L;

    static final ThreadFactory INSTANCE = new LazyThreadFactory();

    private transient volatile ThreadFactory factory;

    private LazyThreadFactory() {
    }

    private ThreadFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = ComponentRegistry.getInstance().getComponent(Keys.THREAD_FACTORY);
                }
            }
        }
        return factory;
    }

    Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

    @SuppressWarnings("NullableProblems")
    public Thread newThread(final Runnable runnable) {
        return getFactory().newThread(Wrappers.wrap(runnable));
    }
}