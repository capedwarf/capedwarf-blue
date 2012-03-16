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

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
@RunWith(Arquillian.class)
public class LoggingTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml");
    }

    @Test
    public void testLogging() {
        Logger log = Logger.getLogger("TestLogger");
        log.info("hello");

        Iterable<RequestLogs> iterable = LogServiceFactory.getLogService().fetch(new LogQuery());
        Assert.assertTrue(iterable.iterator().hasNext());

        for (RequestLogs requestLogs : iterable) {
            List<AppLogLine> appLogLines = requestLogs.getAppLogLines();
            for (AppLogLine appLogLine : appLogLines) {
                if ("hello".equals(appLogLine.getLogMessage())) {
                    return; // test passes
                }
            }
        }
        Assert.fail("Did not find 'hello' in logs.");
    }
}
