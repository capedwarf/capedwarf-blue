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
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossXMPPService implements XMPPService {

    private PresenceConverter presenceConverter = new PresenceConverter();
    private MessageConverter messageConverter = new MessageConverter();

    private XMPPConnection getConnection() {
        return XMPPConnectionManager.getInstance().getConnection();
    }

    public Presence getPresence(JID jid) {
        return getPresence(jid, null);
    }

    public Presence getPresence(JID jid, JID fromJid) {
        return presenceConverter.convert(getConnection().getRoster().getPresence(jid.getId()));
    }

    public void sendPresence(JID jid, PresenceType presenceType, PresenceShow presenceShow, String status) {
        sendPresence(jid, presenceType, presenceShow, status, null);
    }

    public void sendPresence(JID jid, PresenceType presenceType, PresenceShow presenceShow, String status, JID fromJid) {
        getConnection().sendPacket(presenceConverter.convertPresence(presenceType, presenceShow, status));
    }

    public void sendInvitation(JID jid) {
        sendInvitation(jid, null);
    }

    public void sendInvitation(JID jid, JID fromJid) {
        try {
            Roster roster = getConnection().getRoster();
            roster.createEntry(jid.getId(), jid.getId(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SendResponse sendMessage(Message message) {
        getConnection().sendPacket(messageConverter.convert(message));
        return new SendResponse();
    }

    public Message parseMessage(HttpServletRequest request) throws IOException {
        return getParser(request).parseMessage();
    }

    public Presence parsePresence(HttpServletRequest request) throws IOException {
        return getParser(request).parsePresence();
    }

    public Subscription parseSubscription(HttpServletRequest request) throws IOException {
        return getParser(request).parseSubscription();
    }

    private HttpServletRequestMessageParser getParser(HttpServletRequest request) {
        return new HttpServletRequestMessageParser(request);
    }
}
