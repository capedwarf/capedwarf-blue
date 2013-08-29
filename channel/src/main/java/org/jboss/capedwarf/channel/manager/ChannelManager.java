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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.jboss.capedwarf.channel.ExposedChannelService;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ChannelManager {

    public static final String CHANNEL_ENTITY_KIND = "Channel";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_EXPIRATION_TIME = "expirationTime";
    public static final String PROPERTY_TOKEN = "token";

    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    public Channel createChannel(String clientId, int durationMinutes) {
        Entity entity = new Entity(CHANNEL_ENTITY_KIND);
        entity.setProperty(PROPERTY_CLIENT_ID, clientId);
        entity.setProperty(PROPERTY_EXPIRATION_TIME, toExpirationTime(durationMinutes));
        entity.setProperty(PROPERTY_TOKEN, generateToken());
        datastoreService.put(entity);
        return entityToChannel(entity);
    }

    private String generateToken() {
        return String.valueOf(new Random().nextLong());
    }

    private long toExpirationTime(int durationMinutes) {
        return System.currentTimeMillis() + (durationMinutes * 60 * 1000);
    }

    private Channel entityToChannel(Entity entity) {
        return new Channel(
                entity.getKey(),
                (String) entity.getProperty(PROPERTY_CLIENT_ID),
                (Long) entity.getProperty(PROPERTY_EXPIRATION_TIME),
                (String) entity.getProperty(PROPERTY_TOKEN));
    }

    public Set<Channel> getChannels(String clientId) {
        Query query = new Query(CHANNEL_ENTITY_KIND).setFilter(new Query.FilterPredicate(PROPERTY_CLIENT_ID, Query.FilterOperator.EQUAL, clientId));
        List<Entity> entities = datastoreService.prepare(query).asList(FetchOptions.Builder.withDefaults());
        Set<Channel> set = new HashSet<Channel>();
        for (Entity entity : entities) {
            set.add(entityToChannel(entity));
        }
        return set;
    }

    public Channel getChannelByToken(String token) {
        if (token == null) {
            throw new NullPointerException("token should not be null");
        }
        Query query = new Query(CHANNEL_ENTITY_KIND).setFilter(new Query.FilterPredicate(PROPERTY_TOKEN, Query.FilterOperator.EQUAL, token));
        Entity entity = datastoreService.prepare(query).asSingleEntity();
        if (entity == null) {
            throw new NoSuchChannelException("No channel with token " + token);
        }
        return entityToChannel(entity);
    }

    public static ChannelManager getInstance() {
        return ((ExposedChannelService) ChannelServiceFactory.getChannelService()).getChannelManager();
    }

}
