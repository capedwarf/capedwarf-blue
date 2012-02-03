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

import org.jboss.capedwarf.common.jms.ServletRequestCreator;
import org.jboss.capedwarf.common.servlet.AbstractHttpServletRequest;

import javax.jms.Message;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import java.util.Enumeration;

/**
 * Tasks servlet request creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TasksServletRequestCreator implements ServletRequestCreator {

    static final String DELIMITER = "||";
    static final String HEADERS = "task_option_headers_";
    static final String PARAMS = "task_option_params_";

    public ServletRequest createServletRequest(ServletContext context, Message message) throws Exception {
        final TasksServletRequest request = new TasksServletRequest(context);
        final Enumeration names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement().toString();
            final String value = message.getStringProperty(name);
            if (name.startsWith(HEADERS)) {
                request.addHeaders(name.substring(HEADERS.length()), value.split(DELIMITER));
            } else if (name.startsWith(PARAMS)) {
                request.setParameters(name.substring(PARAMS.length()), new String[]{value});
            }
        }
        return request;
    }

    private static class TasksServletRequest extends AbstractHttpServletRequest {
        private TasksServletRequest(ServletContext context) {
            super(context);
        }
    }
}
