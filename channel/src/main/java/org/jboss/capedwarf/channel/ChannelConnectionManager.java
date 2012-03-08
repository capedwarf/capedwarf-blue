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

import com.google.appengine.api.channel.ChannelServiceFactory;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ChannelConnectionManager {

    private Map<String, ChannelConnection> connections = new HashMap<String, ChannelConnection>();

    public static ChannelConnectionManager getInstance() {
        return null;    // TODO
    }

    public ChannelConnection getChannelConnection(String channelToken) {
        return connections.get(channelToken);
    }

    public ChannelConnection createChannelConnection(String channelToken) {
        ChannelManager channelManager = ((JBossChannelService) ChannelServiceFactory.getChannelService()).getChannelManager();

        Channel channel = channelManager.getChannelByToken(channelToken);
        channel.setConnectedNode(InfinispanUtils.getLocalNode());

        ChannelConnection connection = new ChannelConnection();
        connections.put(channelToken, connection);
        return connection;
    }
}
