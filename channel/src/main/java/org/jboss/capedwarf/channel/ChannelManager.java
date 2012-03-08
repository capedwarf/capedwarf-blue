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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

import java.util.Random;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ChannelManager {

    public static final String CHANNEL_ENTITY_KIND = "Channel";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_EXPIRATION_TIME = "expirationTime";
    public static final String PROPERTY_TOKEN = "token";

    public Channel createChannel(String clientId, int durationMinutes) {
        Channel channel = new Channel(clientId, toExpirationTime(durationMinutes), generateToken());
        Entity entity = channelToEntity(channel);
        DatastoreServiceFactory.getDatastoreService().put(entity);
        return channel;
    }

    private String generateToken() {
        return String.valueOf(new Random().nextLong());
    }

    private long toExpirationTime(int durationMinutes) {
        return System.currentTimeMillis() + (durationMinutes * 60 * 1000);
    }

    private Entity channelToEntity(Channel channel) {
        Entity entity = new Entity(CHANNEL_ENTITY_KIND);
        entity.setProperty(PROPERTY_CLIENT_ID, channel.getClientId());
        entity.setProperty(PROPERTY_EXPIRATION_TIME, channel.getExpirationTime());
        entity.setProperty(PROPERTY_TOKEN, channel.getToken());
        return entity;
    }

    private Channel entityToChannel(Entity entity) {
        return new Channel(
                (String) entity.getProperty(PROPERTY_CLIENT_ID),
                (Long) entity.getProperty(PROPERTY_EXPIRATION_TIME),
                (String) entity.getProperty(PROPERTY_TOKEN));
    }

    public Channel getChannel(String clientId) {
        Query query = new Query(CHANNEL_ENTITY_KIND).addFilter(PROPERTY_CLIENT_ID, Query.FilterOperator.EQUAL, clientId);
        Entity entity = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
        return entityToChannel(entity);
    }

    public Channel getChannelByToken(String token) {
        Query query = new Query(CHANNEL_ENTITY_KIND).addFilter(PROPERTY_TOKEN, Query.FilterOperator.EQUAL, token);
        Entity entity = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
        return entityToChannel(entity);
    }

    public void sendMessage(Channel channel, String message) {
        ClusterUtils.submitToNode(channel.getConnectedNode(), new SendMessageTask(channel.getToken(), message));
    }
}
