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


import com.google.appengine.api.xmpp.MessageType;
import org.jivesoftware.smack.packet.Message;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class MessageConverter {

    public Message convert(com.google.appengine.api.xmpp.Message gaeMessage) {
        Message smackMessage = new Message();
        smackMessage.setFrom(gaeMessage.getFromJid().getId());
        smackMessage.setType(convertType(gaeMessage.getMessageType()));
        smackMessage.setTo(gaeMessage.getRecipientJids()[0].getId());   // TODO
        smackMessage.setBody(gaeMessage.getBody());
        return smackMessage;
    }

    private Message.Type convertType(MessageType messageType) {
        switch (messageType) {
            case CHAT:
                return Message.Type.chat;
            case NORMAL:
                return Message.Type.normal;
            case ERROR:
                return Message.Type.error;
            case GROUPCHAT:
                return Message.Type.groupchat;
            case HEADLINE:
                return Message.Type.headline;
            default:
                throw new IllegalArgumentException("Unknown MessageType: " + messageType);
        }
    }
}
