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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class LoggingConfigurationTestCase extends AbstractLoggingTest {

    @Deployment
    public static Archive getDeployment() {
        final TestContext context = new TestContext().setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClass(AbstractLoggingTest.class);
        war.addAsWebInfResource("logging.properties");
        return war;
    }

    @Test
    public void testLoggingPropertiesAreHonored() {
        String infoHello = "info hello";
        String severeHello = "severe hello";

        assertLogDoesntContain(infoHello);
        assertLogDoesntContain(severeHello);

        Logger log = Logger.getLogger(LoggingConfigurationTestCase.class.getName());
        log.info(infoHello);
        log.severe(severeHello);
        flush(log);

        assertLogDoesntContain(infoHello);
        assertLogContains(severeHello);
    }

}
