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

package org.jboss.capedwarf.channel.transport;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.capedwarf.channel.manager.ChannelQueue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public abstract class AbstractTransport implements ChannelTransport {
    protected final Logger log = Logger.getLogger(getClass().getName());

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String channelToken;
    private final ChannelQueue queue;

    public AbstractTransport(HttpServletRequest request, HttpServletResponse response, String channelToken, ChannelQueue queue) {
        this.request = request;
        this.response = response;
        this.channelToken = channelToken;
        this.queue = queue;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getChannelToken() {
        return channelToken;
    }

    public ChannelQueue getQueue() {
        return queue;
    }
}
