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

package org.jboss.capedwarf.tasks;

import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.jms.ServletRequestCreator;

import javax.jms.Message;
import javax.jms.Session;

/**
 * Tasks message creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TasksMessageCreator implements MessageCreator {

    private TaskOptions taskOptions;

    public TasksMessageCreator(TaskOptions taskOptions) {
        if (taskOptions == null)
            throw new IllegalArgumentException("Null task options");
        this.taskOptions = taskOptions;
    }

    public Message createMessage(Session session) {
        return null;
    }

    public void enhanceMessage(Message message) {
        // TODO
    }

    public String getPath() {
        return "/_ah/admin"; // TODO
    }

    public Class<? extends ServletRequestCreator> getServletRequestCreator() {
        return TasksServletRequestCreator.class;
    }
}
