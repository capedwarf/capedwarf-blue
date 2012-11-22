/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.channel;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import org.jboss.capedwarf.channel.manager.Channel;
import org.jboss.capedwarf.channel.manager.ChannelManager;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfChannelService implements ChannelService {

    public static final int DEFAULT_DURATION_MINUTES = 120;

    private ChannelManager channelManager = new ChannelManager();
    private IncomingChannelRequestParser parser = new IncomingChannelRequestParser();

    public String createChannel(String clientId) {
        return createChannel(clientId, DEFAULT_DURATION_MINUTES);
    }

    public String createChannel(String clientId, int durationMinutes) {
        Channel channel = channelManager.createChannel(clientId, durationMinutes);
        return channel.getToken();
    }

    public void sendMessage(ChannelMessage message) {
        for (Channel channel : channelManager.getChannels(message.getClientId())) {
            channel.sendMessage(message.getMessage());
        }
    }

    public ChannelMessage parseMessage(HttpServletRequest request) {
        return parser.parseMessage(request);
    }

    public ChannelPresence parsePresence(HttpServletRequest request) throws IOException {
        return parser.parsePresence(request);
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }
}
