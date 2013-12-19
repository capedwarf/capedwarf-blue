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

package org.jboss.test.capedwarf.testsuite;

import java.io.File;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LibUtils {
    private static ThreadLocal<String> tlModule = new ThreadLocal<String>();
    private static PomEquippedResolveStage resolver;

    public static void addGaeAsLibrary(WebArchive war) {
        addLibrary(war, "com.google.appengine:appengine-api-1.0-sdk");
    }

    public static void addJpaSpecLibrary(WebArchive war) {
        addLibrary(war, "org.hibernate.javax.persistence:hibernate-jpa-2.0-api");
    }

    public static void addObjectifyLibrary(WebArchive war) {
        addLibrary(war, "com.googlecode.objectify:objectify");
    }

    public static void addGuavaLibrary(WebArchive war) {
        addLibrary(war, "com.google.guava:guava");
    }

    public static void addLibrary(WebArchive war, String groupId, String artifactId) {
        addLibrary(war, groupId + ":" + artifactId);
    }

    public static void addLibrary(WebArchive war, String coordinate) {
        war.addAsLibraries(getDependency(coordinate));
    }

    public static void applyTempModule(String module) {
        if (module != null) {
            tlModule.set(module);
        } else {
            tlModule.remove();
        }
    }

    // ------------

    protected synchronized static PomEquippedResolveStage getResolver() {
        if (resolver == null)
            resolver = Maven.resolver().loadPomFromFile(getPomPath());
        return resolver;
    }

    protected static boolean isBlue(String absolutePath) {
        // Is this test run in CapeDwarf Blue -- impl detail!
        // Found "blue" or force Blue via "capedwarf.blue" system property
        return absolutePath.contains("blue") || Boolean.getBoolean("capedwarf.blue");
    }

    // we need testsuite/pom.xml file
    protected static String getPomPath() {
        String module = tlModule.get();
        if (module == null)
            module = "testsuite";
        return buildPomPath(module);
    }

    protected static String buildPomPath(String module) {
        final File root = new File(".");
        String path = "pom.xml";
        final String absolutePath = root.getAbsolutePath();
        if (isBlue(absolutePath)) {
            if (absolutePath.contains(module) == false)
                path = module + "/" + path;
        } else {
            // Or are we in CapeDwarf Testsuite Embedded
            if (absolutePath.contains("embedded") == false)
                path = "embedded/" + path;
        }
        return path;
    }

    private static File getDependency(final String coordinates){
        return getResolver().resolve(coordinates).withoutTransitivity().asSingle(File.class);
    }
}
