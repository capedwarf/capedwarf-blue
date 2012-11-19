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

package org.jboss.test.capedwarf.common.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Base test class for all CapeDwarf tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BaseTest {
    protected static final long DEFAULT_SLEEP = 3000L;

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected static WebArchive getCapedwarfDeployment(TestContext context) {
        final WebArchive war;

        String archiveName = context.getArchiveName();
        if (archiveName != null) {
            if (archiveName.endsWith(".war") == false) archiveName += ".war";
            war = ShrinkWrap.create(WebArchive.class, archiveName);
        } else {
            war = ShrinkWrap.create(WebArchive.class);
        }

        // this class + test_context
        war.addClass(BaseTest.class);
        war.addClass(TestContext.class);

        // web.xml
        if (context.getWebXmlFile() != null) {
            war.setWebXML(context.getWebXmlFile());
        } else {
            war.setWebXML(new StringAsset(context.getWebXmlContent()));
        }

        // jboss-web.xml
        if (context.isContextRoot()) {
            war.addAsWebInfResource("jboss-web.xml");
        }

        // appengine-web.xml
        if (context.getAppEngineWebXmlFile() != null) {
            war.addAsWebInfResource(context.getAppEngineWebXmlFile(), "appengine-web.xml");
        } else {
            war.addAsWebInfResource("appengine-web.xml");
        }

        // capedwarf-compatibility
        if (context.getCompatibilityProperties() != null) {
            war.addAsResource("capedwarf-compatibility.properties");
        } else if (context.getProperties().isEmpty() == false) {
            final StringWriter writer = new StringWriter();
            try {
                context.getProperties().store(writer, "CapeDwarf testing!");
            } catch (IOException e) {
                throw new RuntimeException("Cannot write compatibility properties.", e);
            }
            final StringAsset asset = new StringAsset(writer.toString());
            war.addAsResource(asset, "capedwarf-compatibility.properties");
        }

        if (context.hasCallbacks()) {
            war.addAsWebInfResource("META-INF/datastorecallbacks.xml", "classes/META-INF/datastorecallbacks.xml");
        }

        return war;
    }

    protected static WebArchive getCapedwarfDeployment() {
        return getCapedwarfDeployment(TestContext.DEFAULT);
    }

    protected static boolean isJBossImpl(Object service) {
        if (service == null)
            throw new IllegalArgumentException("Null service!");

        // good enough?
        return service.getClass().getName().contains(".jboss.");
    }

    protected static void sync() {
        sync(DEFAULT_SLEEP);
    }

    protected static void sync(final long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}
