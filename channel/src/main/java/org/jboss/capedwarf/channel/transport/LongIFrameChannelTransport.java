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

import org.jboss.capedwarf.channel.manager.ChannelQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class LongIFrameChannelTransport implements ChannelTransport {

    private final Logger log = Logger.getLogger(getClass().getName());

    public void serveMessages(HttpServletRequest req, HttpServletResponse resp, String channelToken, ChannelQueue queue) throws IOException {
        log.info("Channel queue opened.");
        resp.setContentType("text/html");
        resp.setHeader("Transfer-Encoding", "chunked");

        PrintWriter writer = resp.getWriter();

        writeMessage(writer, channelToken, "open", "");


        long startTime = System.currentTimeMillis();
        while (true) {
            long timeActive = System.currentTimeMillis() - startTime;
            long timeLeft = MAX_CONNECTION_DURATION - timeActive;
            if (timeLeft < 0) {
                break;
            }
            try {
                log.info("Waiting for message (for " + timeLeft + "ms)");
                List<String> messages = queue.getPendingMessages(timeLeft);
                for (String message : messages) {
                    log.info("Received message " + message);
                    writeMessage(writer, channelToken, "message", message);
                }
            } catch (InterruptedException e) {
                // ignored
            }
        }

        writeMessage(writer, channelToken, "close", "");
    }

    private void writeMessage(PrintWriter writer, String channelToken, String type, String message) {
        log.info("Sending message to browser: type=" + type + "; message=" + message);
        writer.println("<script language=\"JavaScript\" type=\"text/javascript\">");
        writer.println("parent.handleChannelMessage(\"" + channelToken + "\", \"" + type + "\", \"" + message + "\");");
        writer.println("</script>");
        writer.flush();
    }

}
