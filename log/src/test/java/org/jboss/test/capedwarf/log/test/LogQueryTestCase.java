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

package org.jboss.test.capedwarf.log.test;

import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.log.LogService.LogLevel.DEBUG;
import static com.google.appengine.api.log.LogService.LogLevel.ERROR;
import static com.google.appengine.api.log.LogService.LogLevel.WARN;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class LogQueryTestCase extends AbstractLoggingTest {

    private Logger log;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    public LogQueryTestCase() {
        super(false);
    }

    @Before
    public void setUp() throws Exception {
        log = Logger.getLogger(LogQueryTestCase.class.getName());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        clear();
    }

    @Test
    @InSequence(1)
    public void createCompleteRequest1() throws Exception {
        log.info("info_createCompleteRequest1");
        log.warning("warning_createCompleteRequest1");
        flush(log);
    }

    @Test
    @InSequence(2)
    public void createCompleteRequest2() throws Exception {
        log.severe("severe_createCompleteRequest2");
        flush(log);
    }

    @Test
    @InSequence(3)
    public void createCompleteRequest3() throws Exception {
        log.info("info_createCompleteRequest3");
        flush(log);
    }

    @Test
    @InSequence(20)
    public void testMinLogLevel() throws Exception {
        LogQuery debugLogQuery = new LogQuery().includeAppLogs(true).minLogLevel(DEBUG);
        assertLogQueryReturns("info_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", debugLogQuery);
        assertLogQueryReturns("info_createCompleteRequest3", debugLogQuery);

        LogQuery warnLogQuery = new LogQuery().includeAppLogs(true).minLogLevel(WARN);
        assertLogQueryReturns("info_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", warnLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", warnLogQuery);

        LogQuery errorLogQuery = new LogQuery().includeAppLogs(true).minLogLevel(ERROR);
        assertLogQueryReturns("severe_createCompleteRequest2", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("warning_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", errorLogQuery);
    }

    @Test
    @InSequence(20)
    public void testIncomplete() throws Exception {
        // GAE dev server doesn't handle this
        if (isRunningInsideGaeDevServer()) {
            return;
        }

        log.info("log message in incomplete request");
        flush(log);

        assertLogQueryReturns("log message in incomplete request", new LogQuery().includeAppLogs(true).includeIncomplete(true));

        assertLogQueryReturns("info_createCompleteRequest1", new LogQuery().includeAppLogs(true).includeIncomplete(true));
        assertLogQueryDoesNotReturn("log message in incomplete request", new LogQuery().includeAppLogs(true).includeIncomplete(false));
    }

}
