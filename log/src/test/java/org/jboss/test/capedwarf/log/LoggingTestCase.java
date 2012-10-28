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

package org.jboss.test.capedwarf.log;

import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class LoggingTestCase extends AbstractLoggingTest {

    @Deployment
    public static Archive getDeployment() {
        return getCapedwarfDeployment().addClass(AbstractLoggingTest.class);
    }

    @Test
    public void testLogging() {
        assertLogDoesntContain("hello");

        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info("hello");
        flush(log);

        assertLogContains("hello");
    }

    @Test
    public void testLogLinesAreReturnedOnlyWhenRequested() {
        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info("hello");
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
}
