/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.xmpp.test;

import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class XMPPServiceTestBase extends TestBase {
    private static final String CAPEDWARF_WEB =
            "<capedwarf-web-app>" +
            "    <xmpp>" +
            "        <host>talk.l.google.com</host>" +
            "        <port>5222</port>" +
            "        <username>%s</username>" +
            "        <password>%s</password>" +
            "    </xmpp>" +
            "</capedwarf-web-app>";

    protected static WebArchive getDefaultDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(XMPPServiceTestBase.class);

        String username = System.getProperty("capedwarf.xmpp.username", "capedwarftest@gmail.com");
        String password = System.getProperty("capedwarf.xmpp.password", "MISSING_PASSWORD");

        Logger.getLogger(XMPPServiceTestBase.class.getName()).info(String.format("XMPP: %s / %s", username, password));

        String cdw = String.format(CAPEDWARF_WEB, username, password);
        war.addAsWebInfResource(new StringAsset(cdw), "capedwarf-web.xml");

        return war;
    }
}
