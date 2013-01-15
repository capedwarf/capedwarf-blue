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

package org.jboss.capedwarf.datastore;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class SecurityActions {
    static ClassLoader setThreadContextClassLoader(ClassLoader cl) {
        if (System.getSecurityManager() == null) {
            return SetThreadContextClassLoaderAction.NON_PRIVILEGED.setThreadContextClassLoader(cl);
        } else {
            return SetThreadContextClassLoaderAction.PRIVILEGED.setThreadContextClassLoader(cl);
        }
    }

    private interface SetThreadContextClassLoaderAction {

        ClassLoader setThreadContextClassLoader(ClassLoader cl);

        SetThreadContextClassLoaderAction NON_PRIVILEGED = new SetThreadContextClassLoaderAction() {
            public ClassLoader setThreadContextClassLoader(ClassLoader cl) {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
                return old;
            }
        };

        SetThreadContextClassLoaderAction PRIVILEGED = new SetThreadContextClassLoaderAction() {
            public ClassLoader setThreadContextClassLoader(final ClassLoader cl) {
                return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        ClassLoader old = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(cl);
                        return old;
                    }
                });
            }
        };
    }
}