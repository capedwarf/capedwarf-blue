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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.shared.jms.ServletRequestCreator;

/**
 * Tasks message creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TasksMessageCreator implements MessageCreator {

    private static final String QUEUE_NAME_HEADER = "X-AppEngine-QueueName";
    private static final String TASK_NAME_HEADER = "X-AppEngine-TaskName";
    private static final String TASK_RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    private static final String TASK_ETA = "X-AppEngine-TaskETA";
    private static final String FAIL_FAST = "X-AppEngine-FailFast";

    private final String queueName;
    private final TaskOptionsHelper taskOptions;

    public TasksMessageCreator(String queueName, TaskOptions taskOptions) {
        if (queueName == null)
            throw new IllegalArgumentException("Null queue name");
        if (taskOptions == null)
            throw new IllegalArgumentException("Null task options");

        this.queueName = queueName;
        this.taskOptions = new TaskOptionsHelper(taskOptions);
    }

    public Message createMessage(Session session) throws Exception {
        final byte[] payload = taskOptions.getPayload();
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
        final List<Object> params = taskOptions.getParams();
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
        final Map<String, List<String>> headers = taskOptions.getHeaders();
        final Map<String, String> map = new HashMap<String, String>();
        if (headers != null && headers.size() > 0) {
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
        }
        map.put(QUEUE_NAME_HEADER, queueName);
        map.put(TASK_NAME_HEADER, toHeaderValue(taskOptions.getTaskName()));
        map.put(TASK_RETRY_COUNT, toHeaderValue(taskOptions.getTaskRetryLimit()));
        map.put(TASK_ETA, toHeaderValue(taskOptions.getEtaMillis()));
        map.put(FAIL_FAST, Boolean.FALSE.toString()); // TODO?
        TasksServletRequestCreator.put(message, TasksServletRequestCreator.HEADERS, map);
    }

    public String getPath() {
        return taskOptions.getUrl();
    }

    public Class<? extends ServletRequestCreator> getServletRequestCreator() {
        return TasksServletRequestCreator.class;
    }

    private static String toHeaderValue(Object value) {
        return (value != null) ? value.toString() : null;
    }
}
