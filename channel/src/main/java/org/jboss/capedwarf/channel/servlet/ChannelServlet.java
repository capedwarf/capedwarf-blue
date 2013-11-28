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

package org.jboss.capedwarf.channel.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.capedwarf.channel.manager.ChannelManager;
import org.jboss.capedwarf.channel.manager.ChannelManagerFactory;
import org.jboss.capedwarf.channel.manager.ChannelQueue;
import org.jboss.capedwarf.channel.manager.ChannelQueueManager;
import org.jboss.capedwarf.channel.transport.ChannelTransport;
import org.jboss.capedwarf.channel.transport.ChannelTransportFactory;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@WebServlet(urlPatterns = ChannelServlet.SERVLET_URI + "/*")
public class ChannelServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(getClass().getName());

    public static final String SERVLET_URI = "/_ah/channel";

    private ChannelManager channelManager;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        channelManager = ChannelManagerFactory.getChannelManager();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/jsapi".equals(req.getPathInfo())) {
            serveJavascript(req, resp);
            return;
        }

        String channelToken = req.getParameter("token");
        if (channelToken == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if ("closeChannel".equals(req.getParameter("action"))) {
            closeChannel(req, resp, channelToken);
            return;
        }

        serveChannelMessages(req, resp, channelToken);
    }

    @SuppressWarnings("UnusedParameters")
    private void closeChannel(HttpServletRequest req, HttpServletResponse resp, String channelToken) {
        channelManager.releaseChannel(channelToken);
    }

    private void serveChannelMessages(HttpServletRequest req, HttpServletResponse resp, String channelToken) throws IOException {
        log.info("Opening channel queue for token " + channelToken);

        final ChannelQueue queue = ChannelQueueManager.getInstance().getOrCreateChannelQueue(channelToken);
        if (queue == null) {
            log.severe(String.format("No channel for token: %s ", channelToken));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String transportType = req.getParameter("transport");
        ChannelTransport transport = ChannelTransportFactory.createChannelTransport(transportType, req, resp, channelToken, queue);
        transport.serveMessages();
    }


    @SuppressWarnings("UnusedParameters")
    private void serveJavascript(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/javascript");

        boolean wsDisabled = CompatibilityUtils.getInstance().isEnabled(Compatibility.Feature.DISABLE_WEB_SOCKETS_CHANNEL);
        resp.getOutputStream().write(String.format("var browserSupportsWebSocket = %s;\n", wsDisabled ? false : "\"WebSocket\" in window").getBytes());

        includeScript(resp, "/org/jboss/capedwarf/channel/channelapi.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfChannel.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfChannelManager.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfSocket.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/LongIFrameTransport.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/SuccessiveXmlHttpTransport.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/WebSocketTransport.js");
    }

    private void includeScript(HttpServletResponse resp, String scriptPath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(scriptPath)) {
            IOUtils.copyStream(in, resp.getOutputStream());
        }
    }

}
