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

import java.net.URL;
import java.net.URLEncoder;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.jboss.capedwarf.channel.IncomingChannelRequestParser;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.InboundServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ChannelNotifications {
    private static boolean enabled() {
        AppEngineWebXml config = CapedwarfEnvironment.getThreadLocalInstance().getAppEngineWebXml();
        InboundServices services = config.getInboundServices();
        return (services != null && services.getServices().contains(InboundServices.Service.channel_presence));
    }

    private static void submit(String url, String clientId) {
        try {
            String base = CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl();
            url = base + url;
            url += String.format("?%s=%s", IncomingChannelRequestParser.CLIENT_ID, URLEncoder.encode(clientId));

            URLFetchService service = URLFetchServiceFactory.getURLFetchService();
            HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.POST);
            service.fetchAsync(request);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected static void connect(Channel channel) {
        if (enabled()) {
            submit("/_ah/channel/connected/", channel.getClientId());
        }
    }

    protected static void disconnect(Channel channel) {
        if (enabled()) {
            submit("/_ah/channel/disconnected/", channel.getClientId());
        }
    }
}
