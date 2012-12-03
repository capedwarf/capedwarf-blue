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

import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.common.apiproxy.CapedwarfDelegate;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LazyThreadFactory implements ThreadFactory, Serializable {
    private static final long serialVersionUID = 1L;

    private static String[] defaultJndiNames = {"java:jboss/threads/threadfactory/capedwarf"};
    static final ThreadFactory INSTANCE = new LazyThreadFactory();

    private transient volatile ThreadFactory factory;

    private LazyThreadFactory() {
    }

    private ThreadFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = JndiLookupUtils.lookup("jndi.thread-factory", ThreadFactory.class, defaultJndiNames);
                }
            }
        }
        return factory;
    }

    Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

    public Thread newThread(final Runnable runnable) {
        return getFactory().newThread(new RunnableWrapper(runnable));
    }

    private static class RunnableWrapper implements Runnable {
        private final ClassLoader appCL;
        private final CapedwarfEnvironment env;
        private final Runnable runnable;

        private RunnableWrapper(Runnable runnable) {
            this.appCL = Application.getAppClassloader();
            this.env = CapedwarfEnvironment.getThreadLocalInstance();
            this.runnable = runnable;
        }

        public void run() {
            final ClassLoader old = SecurityActions.setThreadContextClassLoader(appCL);
            try {
                CapedwarfEnvironment.setThreadLocalInstance(env);
                try {
                    final ApiProxy.Delegate previous = ApiProxy.getDelegate();
                    ApiProxy.setDelegate(CapedwarfDelegate.INSTANCE);
                    try {
                        runnable.run();
                    } finally {
                        ApiProxy.setDelegate(previous);
                    }
                } finally {
                    CapedwarfEnvironment.clearThreadLocalInstance();
                }
            } finally {
                SecurityActions.setThreadContextClassLoader(old);
            }
        }
    }

}