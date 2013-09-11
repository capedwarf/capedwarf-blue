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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
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

    private static void submit(String url, String clientId, boolean connected) {
        Queue queue = QueueFactory.getQueue("CAPEDWARF-INTERNAL");

        TaskOptions options = TaskOptions.Builder.withUrl(url);
        options.param(IncomingChannelRequestParser.CONNECTED, String.valueOf(connected));
        options.param(IncomingChannelRequestParser.CLIENT_ID, clientId);

        queue.add(null, options); // no Tx
    }

    protected static void connect(Channel channel) {
        if (enabled()) {
            submit("/_ah/channel/connected/", channel.getClientId(), false);
        }
    }

    protected static void disconnect(Channel channel) {
        if (enabled()) {
            submit("/_ah/channel/disconnected/", channel.getClientId(), true);
        }
    }
}
