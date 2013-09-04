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

import org.jboss.capedwarf.channel.manager.Channel;
import org.jboss.capedwarf.channel.manager.ChannelManager;
import org.jboss.capedwarf.channel.manager.ChannelQueue;
import org.jboss.capedwarf.channel.manager.ChannelQueueManager;
import org.jboss.capedwarf.channel.manager.NoSuchChannelException;
import org.jboss.capedwarf.channel.transport.ChannelTransport;
import org.jboss.capedwarf.channel.transport.ChannelTransportFactory;
import org.jboss.capedwarf.common.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@WebServlet(urlPatterns = ChannelServlet.SERVLET_URI + "/*")
public class ChannelServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(getClass().getName());

    public static final String SERVLET_URI = "/_ah/channel";

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

    private void closeChannel(HttpServletRequest req, HttpServletResponse resp, String channelToken) {
        try {
            Channel channel = ChannelManager.getInstance().getChannelByToken(channelToken);
            channel.close();
        } catch (NoSuchChannelException ex) {
            log.warning("No channel for token " + channelToken);
        }
    }

    private void serveChannelMessages(HttpServletRequest req, HttpServletResponse resp, String channelToken) throws IOException {
        log.info("Opening channel queue for token " + channelToken);

        ChannelQueue queue;
        try {
            queue = ChannelQueueManager.getInstance().getChannelQueue(channelToken);
        } catch (NoSuchChannelException e) {
            log.severe("No channel for token " + channelToken);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String transportType = req.getParameter("transport");
        ChannelTransport transport = ChannelTransportFactory.createChannelTransport(transportType, req, resp, channelToken, queue);
        transport.serveMessages();
    }


    private void serveJavascript(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/javascript");
        includeScript(resp, "/org/jboss/capedwarf/channel/channelapi.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfChannel.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfChannelManager.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/CapedwarfSocket.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/LongIFrameTransport.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/SuccessiveXmlHttpTransport.js");
        includeScript(resp, "/org/jboss/capedwarf/channel/WebSocketTransport.js");
    }

    private void includeScript(HttpServletResponse resp, String scriptPath) throws IOException {
        InputStream in = getClass().getResourceAsStream(scriptPath);
        try {
            IOUtils.copyStream(in, resp.getOutputStream());
        } finally {
            in.close();
        }
    }

}
