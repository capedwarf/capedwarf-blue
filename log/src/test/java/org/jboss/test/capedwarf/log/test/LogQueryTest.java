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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.log.LogService.LogLevel.DEBUG;
import static com.google.appengine.api.log.LogService.LogLevel.ERROR;
import static com.google.appengine.api.log.LogService.LogLevel.WARN;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class LogQueryTest extends LoggingTestBase {

    private Logger log;

    private static String request1Id;
    private static String request2Id;
    private static String request3Id;
    private static long request2Timestamp;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    public LogQueryTest() {
        super(false);
    }

    @Before
    public void setUp() throws Exception {
        log = Logger.getLogger(LogQueryTest.class.getName());
    }

    @Test
    @InSequence(1)
    public void createCompleteRequest1() throws Exception {
        request1Id = getCurrentRequestId();
        log.info("info_createCompleteRequest1");
        log.warning("warning_createCompleteRequest1");
        flush(log);
    }

    @Test
    @InSequence(2)
    public void createCompleteRequest2() throws Exception {
        log.info("info_createCompleteRequest2");
        Thread.sleep(1000); // increase separation between previous log message and request2Timestamp
        request2Timestamp = System.currentTimeMillis();
        request2Id = getCurrentRequestId();
        Thread.sleep(1000); // increase separation between request2Timestamp and following log message
        log.severe("severe_createCompleteRequest2");
        flush(log);
    }

    @Test
    @InSequence(3)
    public void createCompleteRequest3() throws Exception {
        request3Id = getCurrentRequestId();
        log.info("info_createCompleteRequest3");
        flush(log);
    }

    @Test
    @InSequence(20)
    public void testMinLogLevel() throws Exception {
        log.info("info_incompleteRequest");
        flush(log);

        LogQuery debugLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(DEBUG);
        assertLogQueryReturns("info_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("info_createCompleteRequest2", debugLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", debugLogQuery);
        assertLogQueryReturns("info_createCompleteRequest3", debugLogQuery);
        assertLogQueryReturns("info_incompleteRequest", debugLogQuery);

        LogQuery warnLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(WARN);
        assertLogQueryReturns("info_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", warnLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", warnLogQuery);
        assertLogQueryDoesNotReturn("info_incompleteRequest", warnLogQuery);

        LogQuery errorLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(ERROR);
        assertLogQueryReturns("severe_createCompleteRequest2", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("warning_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", errorLogQuery);
        assertLogQueryDoesNotReturn("info_incompleteRequest", errorLogQuery);
    }

    @Test
    @InSequence(20)
    public void testAllLogLinesReturnedForRequestRegardlessOfMinLogLevel() throws Exception {
        LogQuery warnLogQuery = new LogQuery().includeAppLogs(true).minLogLevel(WARN);
        // even though the log line "info_createCompleteRequest1" has a log level of INFO, it must be returned
        // by a query with minLogLevel(WARN), because minLogLevel only determines what _RequestLogs_
        // will be returned. Every RequestLogs returned should always contain all its log lines
        assertLogQueryReturns("info_createCompleteRequest1", warnLogQuery);
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

    @Test
    @InSequence(20)
    public void testRequestIds() throws Exception {
        LogService service = LogServiceFactory.getLogService();

        LogQuery logQuery = new LogQuery().requestIds(Arrays.asList(request1Id, request2Id));
        Iterator<RequestLogs> iterator = service.fetch(logQuery).iterator();
        assertEquals(request1Id, iterator.next().getRequestId());
        assertEquals(request2Id, iterator.next().getRequestId());
        assertFalse(iterator.hasNext());

        logQuery = new LogQuery().requestIds(Arrays.asList(request2Id));
        iterator = service.fetch(logQuery).iterator();
        assertEquals(request2Id, iterator.next().getRequestId());
        assertFalse(iterator.hasNext());
    }

    @Test
    @InSequence(20)
    public void testStartTimeMillis() throws Exception {
        LogQuery logQuery = new LogQuery().startTimeMillis(request2Timestamp);
        Set<String> requestIds = getRequestIds(logQuery);
        assertFalse(requestIds.contains(request1Id));
        assertTrue(requestIds.contains(request2Id));  // request2 ended after request2Timestamp, so it should be included in the result list
        assertTrue(requestIds.contains(request3Id));
    }

    @Test
    @InSequence(20)
    public void testEndTimeMillis() throws Exception {
        LogQuery logQuery = new LogQuery().endTimeMillis(request2Timestamp);
        Set<String> requestIds = getRequestIds(logQuery);
        assertTrue(requestIds.contains(request1Id));
        assertFalse(requestIds.contains(request2Id));  // request2 ended after request2Timestamp, so it should not be included in the result list
        assertFalse(requestIds.contains(request3Id));
    }

    @Test
    @InSequence(20)
    public void testAllLogLinesReturnedForRequestRegardlessOfStartAndEndTimeMillis() throws Exception {
        LogQuery logQuery = new LogQuery().includeAppLogs(true).startTimeMillis(request2Timestamp);
        // even though the log line "info_createCompleteRequest2" was logged before request2Timestamp,
        // it should still be returned by the query, because startTimeMillis only determines what _RequestLogs_
        // will be returned. Every RequestLogs returned should always contain all its log lines
        assertLogQueryReturns("info_createCompleteRequest2", logQuery);
    }

    private Set<String> getRequestIds(LogQuery logQuery) {
        Set<String> requestIds = new HashSet<String>();
        for (RequestLogs requestLogs : LogServiceFactory.getLogService().fetch(logQuery)) {
            requestIds.add(requestLogs.getRequestId());
        }
        return requestIds;
    }

    private String getCurrentRequestId() {
        return (String) ApiProxy.getCurrentEnvironment().getAttributes().get("com.google.appengine.runtime.request_log_id");
    }
}
