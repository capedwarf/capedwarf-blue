/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.channel.manager;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.ProvidedId;
import org.hibernate.search.annotations.TermVector;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Indexed
@ProvidedId
public class ChannelImpl implements Channel, MessagesAdapter, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ChannelImpl.class.getName());

    public static final String CLIENT_ID = "clientId";
    public static final String TOKEN = "token";

    private String clientId;
    private long expirationTime;
    private String token;
    private MessageNotification notification;

    public ChannelImpl(String clientId, long expirationTime, String token) {
        this.clientId = clientId;
        this.expirationTime = expirationTime;
        this.token = token;
    }

    @Field(name = CLIENT_ID, analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    void setNotification(MessageNotification notification) {
        this.notification = notification;
        ChannelManagerImpl.getInstance().storeChannel(this);
        open();
    }

    private void open() {
        try {
            notification.open(this);
        } finally {
            ChannelNotifications.connect(this);
        }
    }

    void close() {
        try {
            ChannelNotifications.disconnect(this);
        } finally {
            notification.close(this);
        }
    }

    public void sendMessage(String message) {
        if (notification != null) {
            notification.notify(this, message);
        } else {
            log.severe(String.format("Missing notification instance, invalid channel callback?!"));
        }
    }

    public List<Message> getPendingMessages() {
        return MessageManagerImpl.getInstance().getPendingMessages(getToken());
    }

    public void ackMessages(List<String> messageIds) {
        MessageManagerImpl.getInstance().ackMessages(messageIds);
    }
}
