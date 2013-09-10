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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.channel.util.ClusterUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class Channel {

    public static final String CHANNEL_MESSAGE = "ChannelMessage";

    private Key channelEntityKey;
    private String clientId;
    private long expirationTime;
    private String token;

    /**
     * The address of the cluster node with which the browser connection is actually established.
     */
    private Address connectedNode;

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public Channel(Key channelEntityKey, String clientId, long expirationTime, String token) {
        this.channelEntityKey = channelEntityKey;
        this.clientId = clientId;
        this.expirationTime = expirationTime;
        this.token = token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Address getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(Address connectedNode) {
        this.connectedNode = connectedNode;
    }

    public void sendMessage(String message) {
        Entity entity = new Entity(CHANNEL_MESSAGE, channelEntityKey);
        entity.setProperty("type", "message");
        entity.setProperty("message", message);
        datastore.put(entity);
        submitTask(new MessageNotificationTask(getToken()));
    }

    public void open() {
        ChannelNotifications.connect(this);
    }

    public void close() {
        try {
            submitTask(new CloseChannelTask(getToken()));
        } finally {
            ChannelNotifications.disconnect(this);
        }
    }

    private void submitTask(Callable<Void> task) {
        ClusterUtils.submitToAllNodes(task);
//        ClusterUtils.submitToNode(getConnectedNode(), task);  TODO: submit only to node which holds the connection for the channel
    }

    public List<Message> getPendingMessages() {
        Query query = new Query(CHANNEL_MESSAGE)
            .setAncestor(channelEntityKey)
            .addSort(Entity.KEY_RESERVED_PROPERTY);

        List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        List<Message> messages = new ArrayList<>();
        for (Entity entity : entities) {
            messages.add(new Message(
                KeyFactory.keyToString(entity.getKey()),
                (String) entity.getProperty("message")));
        }
        return messages;

    }

    public void ackMessages(List<String> messageIds) {
        List<Key> keys = new ArrayList<>(messageIds.size());
        for (String messageId : messageIds) {
            Key key = KeyFactory.stringToKey(messageId);
            keys.add(key);
        }

        datastore.delete(keys);
    }
}
