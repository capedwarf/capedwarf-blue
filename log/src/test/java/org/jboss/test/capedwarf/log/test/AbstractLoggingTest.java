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

import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ales Justin
 * @author Marko Luksa
 */
public class AbstractLoggingTest extends BaseTest {

    protected static TestContext newTextContext() {
        return new TestContext();
    }

    protected static WebArchive getDefaultDeployment(TestContext context) {
        return getCapedwarfDeployment(context).addClass(AbstractLoggingTest.class);
    }

    @Before
    public void before() {
        clear();
    }

    @After
    public void after() {
        clear();
    }

    protected void clear() {
        LogService service = LogServiceFactory.getLogService();
        if (isJBossImpl(service)) {
            final Class<? extends LogService> clazz = service.getClass();
            try {
                Method clearLog = clazz.getMethod("clearLog");
                clearLog.invoke(service);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void flush(Logger log) {
        for (Handler handler : log.getHandlers()) {
            handler.flush();
        }
    }

    protected boolean logContains(String text) {
        Iterable<RequestLogs> iterable = LogServiceFactory.getLogService().fetch(new LogQuery().includeAppLogs(true));
        for (RequestLogs logs : iterable) {
            for (AppLogLine logLine : logs.getAppLogLines()) {
                if (logLine.getLogMessage().contains(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void assertLogDoesntContain(String text) {
        assertFalse("log should not contain '" + text + "', but it does", logContains(text));
    }

    protected void assertLogContains(String text) {
        assertTrue("log should contain '" + text + "', but it does not", logContains(text));
    }
}
