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

package org.jboss.capedwarf.bytecode;

import java.lang.instrument.ClassFileTransformer;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractClassFileTransformer implements ClassFileTransformer {
    private static final Set<String> IGNORED_PACKAGES = new HashSet<String>();

    static {
        IGNORED_PACKAGES.add("org/jboss/capedwarf/");
        IGNORED_PACKAGES.add("org.jboss.capedwarf.");
        IGNORED_PACKAGES.add("com/google/api/server/");
        IGNORED_PACKAGES.add("com.google.api.server.");
        IGNORED_PACKAGES.add("com/google/appengine/spi/");
        IGNORED_PACKAGES.add("com.google.appengine.spi.");
        IGNORED_PACKAGES.add("com/google/appengine/api/");
        IGNORED_PACKAGES.add("com.google.appengine.api.");
        IGNORED_PACKAGES.add("com/google/appengine/tools/");
        IGNORED_PACKAGES.add("com.google.appengine.tools.");
        IGNORED_PACKAGES.add("com/google/apphosting/api/");
        IGNORED_PACKAGES.add("com.google.apphosting.api.");
        IGNORED_PACKAGES.add("com/google/apphosting/base/");
        IGNORED_PACKAGES.add("com.google.apphosting.base.");
        IGNORED_PACKAGES.add("org/junit/");
        IGNORED_PACKAGES.add("org.junit.");
        IGNORED_PACKAGES.add("org/jboss/arquillian/");
        IGNORED_PACKAGES.add("org.jboss.arquillian.");
    }

    protected boolean isIgnoredPackage(String className) {
        for (String pckg : IGNORED_PACKAGES) {
            if (className.startsWith(pckg)) {
                return true;
            }
        }
        return false;
    }
}
