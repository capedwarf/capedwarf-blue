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

package org.jboss.capedwarf.xmpp;

import org.jboss.capedwarf.common.config.JBossEnvironment;
import org.jboss.capedwarf.common.config.XmppConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class XMPPConnectionManager {

    public static final XMPPConnectionManager instance = new XMPPConnectionManager();

    private XMPPConnection connection;

    public static XMPPConnectionManager getInstance() {
        return instance;
    }

    public XMPPConnection getConnection() {
        if (connection == null) {
            try {
                XmppConfiguration xmppConfig = JBossEnvironment.getThreadLocalInstance().getCapedwarfConfiguration().getXmppConfiguration();

                ConnectionConfiguration config = new ConnectionConfiguration(xmppConfig.getHost(), xmppConfig.getPort());
                connection = new XMPPConnection(config);
                connection.connect();
                connection.login(xmppConfig.getUsername(), xmppConfig.getPassword());
                connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            } catch (XMPPException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public void closeConnection() {
        connection.disconnect();
        connection = null;
    }
}
