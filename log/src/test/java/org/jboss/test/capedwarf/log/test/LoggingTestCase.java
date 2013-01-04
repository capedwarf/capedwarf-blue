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

package org.jboss.test.capedwarf.log.test;

import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class LoggingTestCase extends AbstractLoggingTest {

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    @Test
    public void testLogging() {
        String text = "hello_testLogging";
        assertLogDoesntContain(text);

        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info(text);
        flush(log);

        assertLogContains(text);
    }

    @Test
    public void testLoggingHonorsLogLevel() {
        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info("info_test");
        log.warning("warning_test");
        log.severe("severe_test");
        flush(log);

        assertLogContains("info_test", LogService.LogLevel.INFO);
        assertLogContains("warning_test", LogService.LogLevel.WARN);
        assertLogContains("severe_test", LogService.LogLevel.ERROR);
    }

    @Test
    public void testLogLinesAreReturnedOnlyWhenRequested() {
        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info("hello_testLogLinesAreReturnedOnlyWhenRequested");
        flush(log);

        LogService logService = LogServiceFactory.getLogService();

        for (RequestLogs logs : logService.fetch(new LogQuery().includeAppLogs(false))) {
            assertTrue("AppLogLines should be empty", logs.getAppLogLines().isEmpty());
        }

        for (RequestLogs logs : logService.fetch(new LogQuery().includeAppLogs(true))) {
            if (!logs.getAppLogLines().isEmpty()) {
                // if we've found at least one appLogLine, the test passed
                return;
            }
        }
        fail("Should have found at least one appLogLine, but didn't find any");
    }

    @Test
    public void testLogLinesAlwaysStoredInEmptyNamespace() {
        String text = "something logged while namespace not set to empty";
        assertLogDoesntContain(text);

        NamespaceManager.set("some-namespace");
        try {
            Logger log = Logger.getLogger(LoggingTestCase.class.getName());
            log.info(text);
            flush(log);
        } finally {
            NamespaceManager.set("");
        }

        assertLogContains(text);
    }

}
