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

import java.util.Collections;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ChannelEndpoint extends Endpoint {
    protected static String getToken(Session session) {
        List<String> tokens = session.getRequestParameterMap().get("token");
        if (tokens == null || tokens.isEmpty()) {
            // throw new IllegalArgumentException(String.format("Missing token parameter: %s", session.getQueryString()));
            tokens = Collections.singletonList(session.getQueryString().substring(("token=").length())); // TODO - fix this crap!
        }
        return tokens.get(0);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
        String token = getToken(session);
        WebSocketsManagerImpl.getInstance().setSession(token, session);
        session.addMessageHandler(new ChannelMessageHandler(token));
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        String token = getToken(session);
        ChannelManagerImpl.getInstance().releaseChannel(token);
    }

    @Override
    public void onError(Session session, Throwable thr) {
    }

    private class ChannelMessageHandler implements MessageHandler.Whole<String> {
        private final String token;

        private ChannelMessageHandler(String token) {
            this.token = token;
        }

        public void onMessage(String message) {
            if ("close".equalsIgnoreCase(message)) {
                ChannelManagerImpl.getInstance().releaseChannel(token);
            }
        }
    }
}
