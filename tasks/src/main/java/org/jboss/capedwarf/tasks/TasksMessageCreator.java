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
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Message createMessage(Session session) throws Exception {
        final byte[] payload = (byte[]) ReflectionUtils.invokeInstanceMethod(taskOptions, "getPayload");
        if (payload != null && payload.length > 0) {
            final BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(payload);

            enhanceMessage(bytesMessage);

            return bytesMessage;
        } else {
            return null;
        }
    }

    public void enhanceMessage(Message message) throws Exception {
        addHeaders(message);
        addParameters(message);
    }

    @SuppressWarnings("unchecked")
    private void addParameters(Message message) throws JMSException {
        final List<Object> params = (List<Object>) ReflectionUtils.invokeInstanceMethod(taskOptions, "getParams");
        if (params != null && params.size() > 0) {
            final Map<String, String> map = new HashMap<String, String>();
            for (Object param : params) {
                final String key = (String) ReflectionUtils.invokeInstanceMethod(param, "getURLEncodedName");
                final String value = (String) ReflectionUtils.invokeInstanceMethod(param, "getURLEncodedValue");

                String values = map.get(key);
                if (values == null) {
                    values = value;
                } else {
                    values = values + TasksServletRequestCreator.DELIMITER + value;
                }
                map.put(key, values);
            }
            TasksServletRequestCreator.put(message, TasksServletRequestCreator.PARAMS, map);
        }
    }

    @SuppressWarnings("unchecked")
    private void addHeaders(Message message) throws JMSException {
        final Map<String, List<String>> headers = (Map<String, List<String>>) ReflectionUtils.invokeInstanceMethod(taskOptions, "getHeaders");
        if (headers != null && headers.size() > 0) {
            final Map<String, String> map = new HashMap<String, String>();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final StringBuilder builder = new StringBuilder();
                final List<String> list = entry.getValue();
                if (list.isEmpty() == false) {
                    builder.append(list.get(0));
                    for (int i = 1; i < list.size(); i++) {
                        builder.append(TasksServletRequestCreator.DELIMITER).append(list.get(i));
                    }
                }
                final String key = entry.getKey();
                map.put(key, builder.toString());
            }
            TasksServletRequestCreator.put(message, TasksServletRequestCreator.HEADERS, map);
        }
    }

    public String getPath() {
        return taskOptions.getUrl();
    }

    public Class<? extends ServletRequestCreator> getServletRequestCreator() {
        return TasksServletRequestCreator.class;
    }
}
