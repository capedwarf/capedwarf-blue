/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.test.capedwarf.testsuite.config.test;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.capedwarf.common.config.CapedwarfConfiguration;
import org.jboss.capedwarf.common.config.CapedwarfConfigurationParser;
import org.jboss.capedwarf.common.config.XmppConfiguration;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfConfigurationParserTestCase {

    @Test
    public void testParseSingleAdmin() throws Exception {
        String xml = "<capedwarf-web-app>" +
                "    <admin>admin1@email.com</admin>" +
                "</capedwarf-web-app>";

        CapedwarfConfiguration config = parseConfig(xml);

        Assert.assertEquals(1, config.getAdmins().size());
        Assert.assertEquals("admin1@email.com", config.getAdmins().iterator().next());
    }

    private CapedwarfConfiguration parseConfig(String xml) throws IOException {
        return CapedwarfConfigurationParser.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    @Test
    public void testParseMultipleAdmins() throws Exception {
        String xml = "<capedwarf-web-app>" +
                "    <admin>admin1@email.com</admin>" +
                "    <admin>admin2@email.com</admin>" +
                "    <admin>admin3@email.com</admin>" +
                "</capedwarf-web-app>";

        CapedwarfConfiguration config = parseConfig(xml);

        Assert.assertEquals(3, config.getAdmins().size());
        assertTrue(config.getAdmins().contains("admin1@email.com"));
        assertTrue(config.getAdmins().contains("admin2@email.com"));
        assertTrue(config.getAdmins().contains("admin3@email.com"));
    }

    @Test
    public void testParseXmppConfiguration() throws Exception {
        String xml = "<capedwarf-web-app>" +
                "    <xmpp>" +
                "        <host>xmppHost</host>" +
                "        <port>1234</port>" +
                "        <username>xmppUser</username>" +
                "        <password>xmppPass</password>" +
                "    </xmpp>" +
                "</capedwarf-web-app>";

        CapedwarfConfiguration config = parseConfig(xml);
        XmppConfiguration xmppConfig = config.getXmppConfiguration();

        Assert.assertEquals("xmppHost", xmppConfig.getHost());
        Assert.assertEquals(1234, xmppConfig.getPort());
        Assert.assertEquals("xmppUser", xmppConfig.getUsername());
        Assert.assertEquals("xmppPass", xmppConfig.getPassword());
    }
}
