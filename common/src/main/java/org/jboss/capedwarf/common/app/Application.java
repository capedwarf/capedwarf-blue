/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.app;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.google.apphosting.api.ApiProxy;

/**
 * Application info.
 * Hide GAE API usage.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public final class Application {
    // allow to temp override appId setting
    private static ThreadLocal<String> ids = new ThreadLocal<String>();

    private static void setAppIdInternal(String appId) {
        if (appId != null) {
            ids.set(appId);
        } else {
            ids.remove();
        }
    }

    /**
     * Set temp appId.
     * Null to reset it.
     *
     * @param appId appId
     */
    public static void setAppId(final String appId) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            setAppIdInternal(appId);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    setAppIdInternal(appId);
                    return null;
                }
            });
        }
    }

    /**
     * Get app id.
     *
     * @return the app id
     */
    public static String getAppId() {
        String appId = ids.get();
        return (appId != null) ? appId : getJBossEnvironment().getAppId();
    }

    private static ApiProxy.Environment getJBossEnvironment() {
        ApiProxy.Environment environment = ApiProxy.getCurrentEnvironment();
        if (environment == null) {
            throw new NullPointerException("No API environment is registered for this thread.");
        }
        return environment;
    }

    /**
     * Get app's classloader.
     *
     * @return the app's classloader
     */
    public static ClassLoader getAppClassloader() {
        // TCCL should do for now
        return Thread.currentThread().getContextClassLoader();
    }
}
