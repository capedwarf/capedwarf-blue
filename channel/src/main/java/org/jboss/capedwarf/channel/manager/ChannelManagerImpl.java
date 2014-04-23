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

import java.util.List;

import com.google.appengine.api.channel.ChannelMessage;
import org.infinispan.Cache;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.config.CacheName;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ChannelManagerImpl extends AbstractChannelManager {
    private static final ChannelManagerImpl INSTANCE = new ChannelManagerImpl();

    private final Cache<String, ChannelImpl> channels;
    private final QueryHelper helper;

    private ChannelManagerImpl() {
        channels = InfinispanUtils.getCache(AppIdFactory.getAppId(), CacheName.CHANNEL);
        helper = new QueryHelper(channels);
    }

    static ChannelManagerImpl getInstance() {
        return INSTANCE;
    }

    private static String toKey(String key) {
        return "ch_" + key;
    }

    public Channel createChannel(String clientId, int durationMinutes) {
        String token = generateToken();
        ChannelImpl channel = new ChannelImpl(clientId, toExpirationTime(durationMinutes), token);
        storeChannel(channel);
        return channel;
    }

    public void sendMessage(ChannelMessage message) {
        for (Channel channel : getChannelsByClientId(message.getClientId())) {
            channel.sendMessage(message.getMessage());
        }
    }

    public void releaseChannel(String token) {
        ChannelImpl channel = channels.remove(toKey(token));
        if (channel != null) {
            channel.close();
        }
    }

    ChannelImpl getChannelByToken(String token) {
        return channels.get(toKey(token));
    }

    List<Channel> getChannelsByClientId(String clientId) {
        return helper.getChannels(clientId);
    }

    void storeChannel(ChannelImpl channel) {
        channels.put(toKey(channel.getToken()), channel);
    }
}
