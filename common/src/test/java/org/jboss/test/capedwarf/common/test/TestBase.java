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

package org.jboss.test.capedwarf.common.test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;

import static junit.framework.Assert.assertTrue;

/**
 * Base test class for all CapeDwarf tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestBase {
    protected static final long DEFAULT_SLEEP = 3000L;

    protected final Logger log = Logger.getLogger(getClass().getName());

    private static Method isProxyClass;

    static {
        try {
            Class<?> pfc = loadClass("javassist.util.proxy.ProxyFactory");
            isProxyClass = pfc.getMethod("isProxyClass", new Class[]{Class.class});
        } catch (Throwable ignore) {
            isProxyClass = null;
        }
    }

    protected static Class<?> loadClass(String className) throws ClassNotFoundException {
        return TestBase.class.getClassLoader().loadClass(className);
    }

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
        war.addClass(TestBase.class);
        war.addClass(TestContext.class);
        // categories
        war.addPackage(JBoss.class.getPackage());

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
            war.addAsResource(context.getCompatibilityProperties(), "capedwarf-compatibility.properties");
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
        final Class<?> aClass = service.getClass();
        return isProxyClass(aClass) || aClass.getName().contains(".jboss.");
    }

    protected static boolean isProxyClass(Class<?> clazz) {
        try {
            return (isProxyClass != null) && (Boolean) isProxyClass.invoke(null, clazz);
        } catch (Throwable t) {
            return false;
        }
    }

    protected static void assertRegexpMatches(String regexp, String str) {
        assertTrue("Expected to match regexp " + regexp + " but was: " + str, str != null && str.matches(regexp));
    }

    /**
     * Should work in all envs?
     *
     * @return true if in-container, false otherewise
     */
    protected static boolean isInContainer() {
        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            Transaction tx = ds.beginTransaction();
            try {
                return (ds.getCurrentTransaction() != null);
            } finally {
                tx.rollback();
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    protected boolean isRunningInsideGaeDevServer() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development
            && !isRunningInsideCapedwarf();
    }

    protected boolean isRunningInsideCapedwarf() {
        return isJBossImpl(LogServiceFactory.getLogService());
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
