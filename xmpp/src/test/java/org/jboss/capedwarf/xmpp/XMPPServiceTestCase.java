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

import com.google.appengine.api.xmpp.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class XMPPServiceTestCase {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml")
                .addAsWebInfResource("capedwarf-web.xml");
    }

    @Test
    public void testSendMessage() {
        XMPPService service = XMPPServiceFactory.getXMPPService();
        com.google.appengine.api.xmpp.Message message = new MessageBuilder()
                .withRecipientJids(new JID("marko.luksa@gmail.com"))
//                .withRecipientJids(new JID("ales.justin@gmail.com"))
                .withBody("Hello from GAE XMPP")
                .withMessageType(MessageType.CHAT)
                .build();

        service.sendMessage(message);
//        service.sendPresence(new JID("marko.luksa@gmail.com"), PresenceType.AVAILABLE, PresenceShow.NONE, "status");


    }

    @Test
    public void testInvitation() {
        XMPPService service = XMPPServiceFactory.getXMPPService();
        service.sendInvitation(new JID("marko.luksa@gmail.com"));
//        service.sendInvitation(new JID("ales.justin@gmail.com"));
    }

}
