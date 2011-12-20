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

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class PresenceConverter {

    public Presence convert(org.jivesoftware.smack.packet.Presence presence) {
        return new PresenceBuilder()
                .withFromJid(new JID(presence.getFrom()))
                .withToJid(new JID(presence.getTo()))
                .withStatus(presence.getStatus())
                .withPresenceShow(convertPresenceShow(presence.getMode()))
                .withPresenceType(toPresenceType(presence.getType()))
                .build();
    }

    private PresenceShow convertPresenceShow(org.jivesoftware.smack.packet.Presence.Mode mode) {
        switch (mode) {
            case available:
                return PresenceShow.NONE;
            case away:
                return PresenceShow.AWAY;
            case chat:
                return PresenceShow.CHAT;
            case dnd:
                return PresenceShow.DND;
            case xa:
                return PresenceShow.XA;
            default:
                throw new IllegalArgumentException("Unsupported presence mode " + mode);
        }
    }

    public org.jivesoftware.smack.packet.Presence.Mode convertMode(PresenceShow presenceShow) {
        switch (presenceShow) {
            case AWAY:
                return org.jivesoftware.smack.packet.Presence.Mode.away;
            case CHAT:
                return org.jivesoftware.smack.packet.Presence.Mode.chat;
            case DND:
                return org.jivesoftware.smack.packet.Presence.Mode.dnd;
            case NONE:
                return org.jivesoftware.smack.packet.Presence.Mode.available;
            case XA:
                return org.jivesoftware.smack.packet.Presence.Mode.xa;
            default:
                throw new IllegalArgumentException("Unsupported presenceShow " + presenceShow);
        }
    }

    private PresenceType toPresenceType(org.jivesoftware.smack.packet.Presence.Type type) {
        switch (type) {
            case available:
                return PresenceType.AVAILABLE;
//                return PresenceType.PROBE;
//                return PresenceType.UNAVAILABLE;
//                return PresenceType.UNAVAILABLE;
            case subscribe:
            case subscribed:
            case unsubscribe:
            case unsubscribed:
            case unavailable:
            case error:
            default:
                throw new IllegalArgumentException("Unsupported presence type " + type);
        }
    }

    private org.jivesoftware.smack.packet.Presence.Type convertType(PresenceType presenceType) {
        switch (presenceType) {
            case UNAVAILABLE:
                return org.jivesoftware.smack.packet.Presence.Type.unavailable;
            case AVAILABLE:
                return org.jivesoftware.smack.packet.Presence.Type.available;
            default:
                throw new IllegalArgumentException("Unsupported presenceType " + presenceType);
        }
    }

    public org.jivesoftware.smack.packet.Presence convertPresence(PresenceType presenceType, PresenceShow presenceShow, String status) {
        return new org.jivesoftware.smack.packet.Presence(convertType(presenceType), status, 0, convertMode(presenceShow));
    }
}
