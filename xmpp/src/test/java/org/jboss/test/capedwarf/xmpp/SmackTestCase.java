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

package org.jboss.test.capedwarf.xmpp;

import com.google.appengine.api.xmpp.PresenceShow;
import com.google.appengine.api.xmpp.PresenceType;
import org.jboss.capedwarf.xmpp.PresenceConverter;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class SmackTestCase {


    @Ignore
    @Test
    public void test1() throws Exception {

        XMPPConnection connection = openConnection();

        PresenceConverter presenceConverter = new PresenceConverter();
        connection.sendPacket(presenceConverter.convertPresence(PresenceType.AVAILABLE, PresenceShow.NONE, "status"));

        connection.getRoster().createEntry("ales.justin@gmail.com", "justl", null);

//        Message packet = new Message("marko.luksa@gmail.com", Message.Type.chat);
//        packet.setBody("Hello loj");
//        connection.sendPacket(packet);

//        Thread.sleep(5000);
//        connection.sendPacket(presenceConverter.convertPresence(PresenceType.AVAILABLE, PresenceShow.AWAY, "I'm now away"));

//        Thread.sleep(5000);
//        connection.sendPacket(presenceConverter.convertPresence(PresenceType.UNAVAILABLE, PresenceShow.NONE, "bye bye"));

    }

    private XMPPConnection openConnection() throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration("talk.l.google.com", 5222, "Work");
        XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        connection.login("capedwarftest@gmail.com", "jbossownsyou");
        return connection;
    }

}
