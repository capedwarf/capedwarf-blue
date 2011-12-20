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
import org.jboss.capedwarf.common.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class HttpServletRequestMessageParser {

    private HttpServletRequest request;

    public HttpServletRequestMessageParser(HttpServletRequest request) {
        this.request = request;
    }

    public Message parseMessage() throws IOException {
        return new MessageBuilder()
                .withFromJid(getFrom())
                .withRecipientJids(getTo())
                .withMessageType(getMessageType())
                .withBody(getBody())
                .build();
    }

    public Presence parsePresence() throws IOException {
        return new PresenceBuilder()
                .withFromJid(getFrom())
                .withToJid(getTo())
                .withPresenceShow(getPresenceShow())
                .withPresenceType(getPresenceType())
                .withStatus(getStatus())
                .build();
    }

    private PresenceShow getPresenceShow() {
        return PresenceShow.valueOf(request.getParameter("show"));
    }

    private PresenceType getPresenceType() {
        String pathInfo = request.getPathInfo();
        if (pathInfo.endsWith("/available/")) {
            return PresenceType.AVAILABLE;
        } else if (pathInfo.endsWith("/unavailable/")) {
            return PresenceType.UNAVAILABLE;
        } else if (pathInfo.endsWith("/probe/")) {
            return PresenceType.PROBE;
        } else {
            throw new RuntimeException("cannot determine PresenceType from pathInfo: " + pathInfo);
        }
    }

    public Subscription parseSubscription() throws IOException {
        return new SubscriptionBuilder()
                .withFromJid(getFrom())
                .withToJid(getTo())
                .withSubscriptionType(getSubscriptionType())
                .withStanza(getStanza())
                .build();
    }

    private SubscriptionType getSubscriptionType() {
        String pathInfo = request.getPathInfo();
        if (pathInfo.endsWith("/subscribe/")) {
            return SubscriptionType.SUBSCRIBE;
        } else if (pathInfo.endsWith("/subscribed/")) {
            return SubscriptionType.SUBSCRIBED;
        } else if (pathInfo.endsWith("/unsubscribe/")) {
            return SubscriptionType.UNSUBSCRIBE;
        } else if (pathInfo.endsWith("/unsubscribed/")) {
            return SubscriptionType.UNSUBSCRIBED;
        } else {
            throw new RuntimeException("cannot determine SubscriptionType from pathInfo: " + pathInfo);
        }
    }

    private JID getFrom() throws IOException {
        return new JID(getRequestPartAsString("from"));
    }

    private JID getTo() throws IOException {
        return new JID(getRequestPartAsString("to"));
    }

    private MessageType getMessageType() {
        return MessageType.CHAT; // TODO
    }

    private String getBody() throws IOException {
        return getRequestPartAsString("body");
    }

    private String getRequestPartAsString(String partName) throws IOException {
        return request.getParameter(partName);
//        try {
//            return getAsString(request.getPart(partName).getInputStream());
//        } catch (ServletException ex) {
//            throw new IOException(ex);
//        }
    }

    private String getAsString(InputStream stream) throws IOException {
        byte[] bytes = IOUtils.toBytes(stream, true);
        return new String(bytes, "UTF-8");
    }

    public String getStatus() {
        return "";
    }

    public String getStanza() {
        return "";
    }
}
