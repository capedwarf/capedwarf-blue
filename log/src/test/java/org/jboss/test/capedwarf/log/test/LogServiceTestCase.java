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
import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class LogServiceTestCase extends AbstractLoggingTest {

    private LogService service;

    @Before
    public void setUp() throws Exception {
        service = LogServiceFactory.getLogService();
    }

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = newTestContext().setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        return getDefaultDeployment(context)
                .addAsWebInfResource("logging-all.properties", "logging.properties");
    }

    @Test
    public void testLogLevelInAppLogLineMatchesActualLogLevelUsedWhenLogging() {
        Logger log = Logger.getLogger(LogServiceTestCase.class.getName());
        log.finest("finest_testLogLevelMatches");
        log.finer("finer_testLogLevelMatches");
        log.fine("fine_testLogLevelMatches");
        log.config("config_testLogLevelMatches");
        log.info("info_testLogLevelMatches");
        log.warning("warning_testLogLevelMatches");
        log.severe("severe_testLogLevelMatches");
        flush(log);

        assertLogContains("finest_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("finer_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("fine_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("config_testLogLevelMatches", LogService.LogLevel.DEBUG);

        // we can't test the following on dev appserver, because it returns incorrect logLevels
        // more info at http://code.google.com/p/googleappengine/issues/detail?id=8651
        if (!runningInsideDevAppEngine() || isJBossImpl(service)) {
            assertLogContains("info_testLogLevelMatches", LogService.LogLevel.INFO);
            assertLogContains("warning_testLogLevelMatches", LogService.LogLevel.WARN);
            assertLogContains("severe_testLogLevelMatches", LogService.LogLevel.ERROR);
        }
    }

    @Test
    public void testAllKindsOfLogQueries() {
        assertLogQueryExecutes(new LogQuery());
        assertLogQueryExecutes(new LogQuery().minLogLevel(LogService.LogLevel.WARN));
        assertLogQueryExecutes(new LogQuery().includeIncomplete(true));
        assertLogQueryExecutes(new LogQuery().includeAppLogs(true));
        assertLogQueryExecutes(new LogQuery().batchSize(20));
//        assertLogQueryExecutes(new LogQuery().offset());  // TODO
        assertLogQueryExecutes(new LogQuery().majorVersionIds(Arrays.asList("1", "2", "3")));
        assertLogQueryExecutes(new LogQuery().requestIds(Arrays.asList("1", "2", "3")));
        assertLogQueryExecutes(new LogQuery().startTimeMillis(System.currentTimeMillis()));
        assertLogQueryExecutes(new LogQuery().startTimeUsec(1000L * System.currentTimeMillis()));
        assertLogQueryExecutes(new LogQuery().endTimeMillis(System.currentTimeMillis()));
        assertLogQueryExecutes(new LogQuery().endTimeUsec(1000L * System.currentTimeMillis()));
        assertLogQueryExecutes(
            new LogQuery()
                .minLogLevel(LogService.LogLevel.WARN)
                .includeIncomplete(true)
                .includeAppLogs(true)
                .batchSize(20)
//                .offset() // TODO
                .majorVersionIds(Arrays.asList("1", "2", "3"))
                .requestIds(Arrays.asList("1", "2", "3"))
                .startTimeMillis(System.currentTimeMillis())
                .endTimeMillis(System.currentTimeMillis())
        );
    }

    private void assertLogQueryExecutes(LogQuery logQuery) {
        service.fetch(logQuery);
    }

    @Test
    public void testLogLinesAreReturnedOnlyWhenRequested() {
        Logger log = Logger.getLogger(LogServiceTestCase.class.getName());
        log.info("hello_testLogLinesAreReturnedOnlyWhenRequested");
        flush(log);

        for (RequestLogs logs : service.fetch(new LogQuery().includeAppLogs(false))) {
            assertTrue("AppLogLines should be empty", logs.getAppLogLines().isEmpty());
        }

        for (RequestLogs logs : service.fetch(new LogQuery().includeAppLogs(true))) {
            if (!logs.getAppLogLines().isEmpty()) {
                // if we've found at least one appLogLine, the test passed
                return;
            }
        }
        fail("Should have found at least one appLogLine, but didn't find any");
    }

}
