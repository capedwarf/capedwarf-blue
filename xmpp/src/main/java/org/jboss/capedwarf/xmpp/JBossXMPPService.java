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

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.PresenceShow;
import com.google.appengine.api.xmpp.PresenceType;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.Subscription;
import com.google.appengine.api.xmpp.XMPPService;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossXMPPService implements XMPPService {
    private static final Logger log = Logger.getLogger(JBossXMPPService.class.getName());

    private static final PresenceConverter presenceConverter = new PresenceConverter();
    private static final MessageConverter messageConverter = new MessageConverter();

    public Presence getPresence(JID jid) {
        return getPresence(jid, null);
    }

    public Presence getPresence(final JID jid, JID fromJid) {
        final ConnectionAction<org.jivesoftware.smack.packet.Presence> action = new ConnectionAction<org.jivesoftware.smack.packet.Presence>() {
            public org.jivesoftware.smack.packet.Presence execute(XMPPConnection connection) {
                return connection.getRoster().getPresence(jid.getId());
            }
        };
        return presenceConverter.convert(execute(action));
    }

    public void sendPresence(JID jid, PresenceType presenceType, PresenceShow presenceShow, String status) {
        sendPresence(jid, presenceType, presenceShow, status, null);
    }

    public void sendPresence(JID jid, final PresenceType presenceType, final PresenceShow presenceShow, final String status, JID fromJid) {
        final ConnectionAction<Void> action = new ConnectionAction<Void>() {
            public Void execute(XMPPConnection connection) {
                connection.sendPacket(presenceConverter.convertPresence(presenceType, presenceShow, status));
                return null;
            }
        };
        execute(action);
    }

    public void sendInvitation(JID jid) {
        sendInvitation(jid, null);
    }

    public void sendInvitation(final JID jid, JID fromJid) {
        final ConnectionAction<Void> action = new ConnectionAction<Void>() {
            public Void execute(XMPPConnection connection) {
                try {
                    Roster roster = connection.getRoster();
                    if (roster != null) {
                        roster.createEntry(jid.getId(), jid.getId(), null);
                    } else {
                        log.warning("XMPP roster is null, cannot send invitation.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        execute(action);
    }

    public SendResponse sendMessage(final Message message) {
        final ConnectionAction<SendResponse> action = new ConnectionAction<SendResponse>() {
            public SendResponse execute(XMPPConnection connection) {
                connection.sendPacket(messageConverter.convert(message));
                final SendResponse response = new SendResponse();
                for (JID rjid : message.getRecipientJids()) {
                    response.addStatus(rjid, SendResponse.Status.SUCCESS);
                }
                return response;
            }
        };
        return execute(action);
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

    private static <T> T execute(ConnectionAction<T> action) {
        final XMPPConnectionManager manager = XMPPConnectionManager.getInstance();
        final XMPPConnection connection = manager.createConnection();
        try {
            return action.execute(connection);
        } finally {
            manager.destroyConnection(connection);
        }
    }

    private static interface ConnectionAction<T> {
        T execute(XMPPConnection connection);
    }
}
