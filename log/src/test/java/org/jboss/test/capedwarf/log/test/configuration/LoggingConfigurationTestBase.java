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

package org.jboss.test.capedwarf.log.test.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.log.test.LoggingTestBase;

/**
 * @author Marko Luksa
 */
public abstract class LoggingConfigurationTestBase extends LoggingTestBase {

    public static final List<Level> LEVELS = Arrays.asList(Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST);

    protected void assertLogOnlyLogsMessagesAboveOrAtLevel(Level minLevel) {
        assertLogOnlyLogsMessagesAboveOrAtLevel(Logger.getLogger(getClass().getName()), minLevel);
    }

    protected void assertLogOnlyLogsMessagesAboveOrAtLevel(Logger log, Level minLevel) {
        long start = System.currentTimeMillis();

        for (Level level : LEVELS) {
            log.log(level, createMessage(level, start));
        }
        flush(log);

        for (Level level : LEVELS) {
            if (level.intValue() >= minLevel.intValue()) {
                assertLogContains(createMessage(level, start));
            } else {
                assertLogDoesntContain(createMessage(level, start));
            }
        }
    }

    private String createMessage(Level lev, long start) {
        return "Log message at level " + lev.getName() + " (" + start + ")";
    }

    protected static Archive getDeploymentWithLoggingLevelSetTo(Level level) {
        return getDeploymentWithLoggingProperties(new StringAsset(".level=" + level.getName()));
    }

    protected static Archive getDeploymentWithLoggingProperties(Asset loggingProperties) {
        TestContext context = newTestContext().setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        WebArchive war = getDefaultDeployment(context);
        war.addClass(LoggingConfigurationTestBase.class);
        war.addAsWebInfResource(loggingProperties, "logging.properties");
        return war;
    }

}
