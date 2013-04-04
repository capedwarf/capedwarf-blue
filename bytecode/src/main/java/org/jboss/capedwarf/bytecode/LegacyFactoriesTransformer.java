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

package org.jboss.capedwarf.bytecode;

/**
 * ONLY legacy service factory transformers go here!!
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public final class LegacyFactoriesTransformer extends MultipleTransformer {

    // -- Keep lexicographical order --

    public LegacyFactoriesTransformer() {
        // GAE API
        register("com.google.appengine.api.appidentity.AppIdentityServiceFactory", new AppIdentityServiceFactoryTransformer());
        register("com.google.appengine.api.blobstore.BlobstoreServiceFactory", new BlobstoreServiceFactoryTransformer());
        register("com.google.appengine.api.capabilities.CapabilitiesServiceFactory", new CapabilitiesServiceFactoryTransformer());
        register("com.google.appengine.api.channel.ChannelServiceFactory", new ChannelServiceFactoryTransformer());
        register("com.google.appengine.api.datastore.DatastoreServiceFactory", new DatastoreServiceFactoryTransformer());
        register("com.google.appengine.api.files.FileServiceFactory", new FileServiceFactoryTransformer());
        register("com.google.appengine.api.images.ImagesServiceFactory", new ImagesServiceFactoryTransformer());
        register("com.google.appengine.api.log.LogServiceFactory", new LogServiceFactoryTransformer());
        register("com.google.appengine.api.mail.MailServiceFactory", new MailServiceFactoryTransformer());
        register("com.google.appengine.api.memcache.MemcacheServiceFactory", new MemcacheServiceFactoryTransformer());
        register("com.google.appengine.api.quota.QuotaServiceFactory", new QuotaFactoryTransformer());
        register("com.google.appengine.api.search.SearchServiceFactory", new SearchServiceFactoryTransformer());
        register("com.google.appengine.api.oauth.OAuthServiceFactory", new OAuthServiceFactoryTransformer());
        register("com.google.appengine.api.taskqueue.QueueFactory", new QueueFactoryTransformer());
        register("com.google.appengine.api.urlfetch.URLFetchServiceFactory", new URLFetchServiceFactoryTransformer());
        register("com.google.appengine.api.users.UserServiceFactory", new UserServiceFactoryTransformer());
        register("com.google.appengine.api.xmpp.XMPPServiceFactory", new XMPPServiceFactoryTransformer());
    }
}
