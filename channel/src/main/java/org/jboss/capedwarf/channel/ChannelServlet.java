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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
@WebServlet(urlPatterns = ChannelServlet.SERVLET_URI + "/*")
public class ChannelServlet extends HttpServlet {

    public static final String SERVLET_URI = "/_ah/channel";

    public static final int MAX_CONNECTION_DURATION = 30000;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String channelToken = req.getParameter("token");
        ChannelConnection connection = ChannelConnectionManager.getInstance().createChannelConnection(channelToken);

        resp.setHeader("Transfer-Encoding", "chunked");
        PrintWriter writer = resp.getWriter();

        long startTime = System.currentTimeMillis();
        while (true) {
            long timeActive = System.currentTimeMillis() - startTime;
            long timeLeft = MAX_CONNECTION_DURATION - timeActive;
            if (timeLeft < 0) {
                break;
            }
            try {
                String message = connection.getPendingMessage(timeLeft);
                if (message != null) {
                    writer.println(message);
                    writer.flush();
                }
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }
}
