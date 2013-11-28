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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.capedwarf.channel.manager.ChannelQueue;
import org.jboss.capedwarf.channel.manager.ChannelQueueClosedException;
import org.jboss.capedwarf.channel.manager.Message;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class SuccessiveXmlHttpChannelTransport extends AbstractTransport {

    public static final String DELIMITER = ":_:";

    public SuccessiveXmlHttpChannelTransport(HttpServletRequest req, HttpServletResponse resp, String channelToken, ChannelQueue queue) {
        super(req, resp, channelToken, queue);
    }

    public void serveMessages() throws IOException {
        log.fine("Channel queue opened.");
        getQueue().ackMessages(getAckMessageIds());

        getResponse().setContentType("text/plain");

        PrintWriter writer = getResponse().getWriter();
        if (isFirstRequest()) {
            writeOpenMessage(writer);
            return;
        }

        serveMessages(writer);
    }

    private List<String> getAckMessageIds() {
        String ackIds = getRequest().getParameter("ackIds");
        if (ackIds == null || ackIds.equals("")) {
            return Collections.emptyList();
        }
        return Arrays.asList(ackIds.split(","));
    }

    private boolean isFirstRequest() {
        int requestIndex = Integer.valueOf(getRequest().getParameter("requestIndex"));
        return requestIndex == 0;
    }

    private void serveMessages(PrintWriter writer) {
        try {
            log.fine(String.format("Waiting for message (for a maximum of %sms)", MAX_CONNECTION_DURATION));
            List<Message> messages = getQueue().getPendingMessages(MAX_CONNECTION_DURATION);
            writeMessages(writer, messages);
        } catch (ChannelQueueClosedException ex) {
            log.info("Channel closed: " + getChannelToken());
            writeCloseMessage(writer);
        } catch (InterruptedException e) {
            log.warning("InterruptedException in serveMessages");
            // ignored
        }
    }

    private void writeMessages(PrintWriter writer, List<Message> messages) {
        for (Message message : messages) {
            writeMessage(writer, "message", message);
        }
    }

    private void writeOpenMessage(PrintWriter writer) {
        writeMessage(writer, "open", Message.NULL);
    }

    private void writeCloseMessage(PrintWriter writer) {
        writeMessage(writer, "close", Message.NULL);
    }

    private void writeMessage(PrintWriter writer, String type, Message message) {
        log.fine(String.format("Sending message to browser: type=%s; message=%s", type, message));
        writer.println(type + DELIMITER + message.getId() + DELIMITER + message.getMessage());
        writer.flush();
    }
}
