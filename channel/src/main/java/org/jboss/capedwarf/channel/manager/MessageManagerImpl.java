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
import java.util.UUID;

import org.infinispan.Cache;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MessageManagerImpl extends MessageManager {
    private static final MessageManagerImpl INSTANCE = new MessageManagerImpl();

    private final Cache<String, Message> messages;
    private final QueryHelper helper;

    private MessageManagerImpl() {
        messages = InfinispanUtils.getCache(AppIdFactory.getAppId(), CacheName.CHANNEL);
        helper = new QueryHelper(messages);
    }

    static MessageManager getInstance() {
        return INSTANCE;
    }

    private static String toKey(String key) {
        return "msg_" + key;
    }

    String storeMessage(Message msg) {
        String id = UUID.randomUUID().toString();
        msg.setId(id);
        messages.put(toKey(id), msg);
        return id;
    }

    List<Message> getPendingMessages(String token) {
        return helper.getPendingMessages(token);
    }

    void ackMessages(List<String> messageIds) {
        for (String id : messageIds) {
            messages.remove(toKey(id));
        }
    }
}
