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

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test async logging.
 *
 * @author Ales Justin
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class AsyncLogTest extends LoggingTestBase {
    @Deployment
    public static WebArchive getDeployment() throws Exception {
        TestContext context = TestContext.withLogging();
        context.getProperties().put("async.logging", "true");
        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(LoggingTestBase.class);
        war.addAsWebInfResource(new StringAsset("queueLength=512"), "logging-async.properties");
        return war;
    }

    @Test
    public void testSmoke() {
        Logger.getLogger(AsyncLogTest.class.getName()).warning("Async!");
        sync();
    }

    @Test
    public void testLogLinesAreReturnedInSameOrderAsTheyWereLogged() throws Exception {
        int NUMBER_OF_LINES = 20;
        for (int i=0; i< NUMBER_OF_LINES; i++) {
            log.log(Level.INFO, "line " + i);
        }
        flush(log);
        Thread.sleep(2000);

        LogQuery logQuery = new LogQuery().requestIds(Collections.singletonList(getCurrentRequestId())).minLogLevel(LogService.LogLevel.INFO).includeAppLogs(true);
        RequestLogs requestLogs = LogServiceFactory.getLogService().fetch(logQuery).iterator().next();

        List<AppLogLine> appLogLines = requestLogs.getAppLogLines();
        assertEquals("number of logged lines", NUMBER_OF_LINES, appLogLines.size());
        for (int i=0; i< NUMBER_OF_LINES; i++) {
            assertEquals("org.jboss.test.capedwarf.log.test.AsyncLogTest testLogLinesAreReturnedInSameOrderAsTheyWereLogged: line " + i + "\n", appLogLines.get(i).getLogMessage());
        }
    }

    private String getCurrentRequestId() {
        return (String) ApiProxy.getCurrentEnvironment().getAttributes().get("com.google.appengine.runtime.request_log_id");
    }

}
