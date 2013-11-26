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

package org.jboss.capedwarf.channel.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ChannelQueueManager {
    private Map<String, ChannelQueue> queues = new HashMap<>();

    private static final ChannelQueueManager instance = new ChannelQueueManager(); // TODO: make this as it should be

    public static ChannelQueueManager getInstance() {
        return instance;
    }

    public synchronized ChannelQueue getOrCreateChannelQueue(String channelToken) {
        ChannelImpl channel = ChannelManagerImpl.getInstance().getChannelByToken(channelToken);
        if (channel == null) {
            return null;
        }

        ChannelQueue queue = queues.get(channelToken);
        if (queue == null) {
            queue = new ChannelQueue(channel);
            queues.put(channelToken, queue);

            channel.setNotification(MessageNotificationType.SIMPLE);
        }
        return queue;
    }

    public synchronized void removeChannelQueue(String channelToken) {
        queues.remove(channelToken);
    }

    public synchronized ChannelQueue getChannelQueue(String channelToken) {
        return queues.get(channelToken);
    }
}
