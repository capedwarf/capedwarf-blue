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

package org.jboss.capedwarf.arquillian;

import java.io.File;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GaeApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    public void process(Archive<?> archive, TestClass testClass) {
        final Node lib = archive.get("WEB-INF/lib");
        if (lib != null) {
            final Set<Node> libs = lib.getChildren();
            for (Node jar : libs) {
                if (jar.getPath().get().contains("appengine-api"))
                    return;
            }
        }

        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            war.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").
                    resolve("com.google.appengine:appengine-api-1.0-sdk").withTransitivity().as(File.class));
        }
    }
}
