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

package org.jboss.capedwarf.common.compatibility;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.components.Key;
import org.jboss.capedwarf.shared.components.SimpleKey;

/**
 * Handle Compatibility.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class CompatibilityUtils {
    private static final Key<Compatibility> KEY = new SimpleKey<Compatibility>(EnvAppIdFactory.INSTANCE, Compatibility.class);

    public static Compatibility getInstance() {
        return getInstance(null);
    }

    public synchronized static Compatibility getInstance(ClassLoader cl) {
        if (cl == null) {
            cl = Application.getAppClassloader();
        }
        return Compatibility.getInstance(cl, KEY);
    }
}
