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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class WebSocketsManagerImpl implements WebSocketsManager {
    private static final Logger log = Logger.getLogger(WebSocketsManagerImpl.class.getName());
    private static final WebSocketsManagerImpl INSTANCE = new WebSocketsManagerImpl();

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private WebSocketsManagerImpl() {
    }

    static WebSocketsManager getInstance() {
        return INSTANCE;
    }

    public void setSession(String token, Session session) {
        sessions.put(token, session);

        ChannelImpl channel = ChannelManagerImpl.getInstance().getChannelByToken(token);
        if (channel != null) {
            channel.setNotification(MessageNotificationType.WEB_SOCKET);
        } else {
            final String msg = String.format("No such channel for token: %s", token);
            log.fine(msg);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, msg));
            } catch (IOException e) {
                log.warning(String.format("Error closing session: %s", e));
            }
        }
    }

    public void sendMessage(String token, String message) {
        Session session = sessions.get(token);
        if (session != null) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.severe(String.format("Error sending message to channel: %s", token));
                }
            } else {
                log.fine(String.format("Session is closed, releasing channel for token: %s", token));
                ChannelManagerImpl.getInstance().releaseChannel(token);
            }
        } else {
            log.warning(String.format("No Session registered for channel: %s", token));
        }
    }

    public void releaseChannel(String token) {
        sessions.remove(token);
    }
}
