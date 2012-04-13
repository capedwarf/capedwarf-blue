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

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ChannelQueue {

    private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    private boolean closed;

    public void send(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            // ignored
        }
    }

    public void close() {
        this.closed = true;
        send("");   // send empty message to unblock queue
    }

    /**
     * Returns all the pending messages in the queue. If there are messages pending, all of those messages are returned.
     * If there are no messages pending, the method blocks up to timeoutMillis for a message to become available.
     *
     * @param timeoutMillis number of milliseconds to wait for a new message
     * @return a list of messages
     * @throws InterruptedException
     */
    public List<String> getPendingMessages(long timeoutMillis) throws InterruptedException {
        List<String> messages = new LinkedList<String>();
        String message = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        if (message != null) {
            messages.add(message);
            if (!queue.isEmpty()) {
                queue.drainTo(messages);
            }
        }
        if (closed) {
            throw new ChannelQueueClosedException();
        }
        return messages;
    }
}
