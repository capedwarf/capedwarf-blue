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

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class IncomingChannelRequestParser {
    public static final String KEY = "key";
    public static final String MSG = "msg";

    public static final String CONNECTED = "connected";
    public static final String CLIENT_ID = "clientId";

    static ChannelMessage parseMessage(HttpServletRequest request) {
        return new ChannelMessage(
                request.getParameter(KEY),
                request.getParameter(MSG)
        );
    }

    static ChannelPresence parsePresence(HttpServletRequest request) {
        boolean isConnected = Boolean.valueOf(request.getParameter(CONNECTED));
        String clientId = request.getParameter(CLIENT_ID);
        return ReflectionUtils.newInstance(ChannelPresence.class, new Class[] {boolean.class, String.class}, new Object[] {isConnected, clientId});
    }
}
