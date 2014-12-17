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

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.capedwarf.common.jms.MessageCreator;
import org.jboss.capedwarf.shared.jms.MessageConstants;
import org.jboss.capedwarf.shared.jms.ServletRequestCreator;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * Tasks message creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class TasksMessageCreator implements MessageCreator {

    public static final String QUEUE_NAME_HEADER = "X-AppEngine-QueueName";
    public static final String TASK_NAME_HEADER = "X-AppEngine-TaskName";
    public static final String TASK_RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String TASK_EXECUTION_COUNT = "X-AppEngine-TaskExecutionCount";
    public static final String TASK_ETA = "X-AppEngine-TaskETA";
    public static final String FAIL_FAST = "X-AppEngine-FailFast";
    public static final String CURRENT_NAMESPACE = "X-AppEngine-Current-Namespace";

    public static final String HDR_SCHEDULED_DELIVERY_TIME = "_HQ_SCHED_DELIVERY";
    public static final String QUEUE_NAME_KEY = "__CD__QueueName__";
    public static final String TASK_NAME_KEY = "__CD__TaskName__";

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
        byte[] payload = taskOptions.getPayload();
        if (payload != null && payload.length > 0) {
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(payload);

            enhanceMessage(bytesMessage);

            return bytesMessage;
        } else {
            return null;
        }
    }

    public void enhanceMessage(Message message) throws Exception {
        addMethod(message);
        addHeaders(message);
        addParameters(message);

        message.setStringProperty(QUEUE_NAME_KEY, queueName);
        setIfNotNull(message, TASK_NAME_KEY, taskOptions.getTaskName());
        message.setIntProperty(MessageConstants.MAX_ATTEMPTS, taskOptions.getTaskRetryLimit() == null ? -1 : taskOptions.getTaskRetryLimit());

        Long etaMillis = taskOptions.getCalculatedEtaMillis();
        if (etaMillis != null) {
            message.setLongProperty(HDR_SCHEDULED_DELIVERY_TIME, etaMillis);
        }
    }

    private void addMethod(Message message) throws JMSException {
        message.setStringProperty(TasksServletRequestCreator.METHOD, taskOptions.getMethod().name());
    }

    @SuppressWarnings("unchecked")
    private void addParameters(Message message) throws JMSException {
        List<Object> params = taskOptions.getParams();
        if (params != null && params.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            for (Object param : params) {
                String key = (String) ReflectionUtils.invokeInstanceMethod(param, "getURLEncodedName");
                String value = (String) ReflectionUtils.invokeInstanceMethod(param, "getURLEncodedValue");

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
        Map<String, List<String>> headers = taskOptions.getHeaders();
        Map<String, String> map = new HashMap<String, String>();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                StringBuilder builder = new StringBuilder();
                List<String> list = entry.getValue();
                if (list.isEmpty() == false) {
                    builder.append(list.get(0));
                    for (int i = 1; i < list.size(); i++) {
                        builder.append(TasksServletRequestCreator.DELIMITER).append(list.get(i));
                    }
                }
                if (builder.length() > 0) {
                    String key = entry.getKey();
                    map.put(key, builder.toString());
                }
            }
        }
        map.put(QUEUE_NAME_HEADER, queueName);
        putIfNotNull(map, TASK_NAME_HEADER, taskOptions.getTaskName());
        putIfNotNull(map, TASK_ETA, taskOptions.getEtaMillis());
        map.put(FAIL_FAST, Boolean.FALSE.toString()); // TODO?
        if (map.containsKey(CURRENT_NAMESPACE) == false) {
            String namespace = getCurrentNamespace();
            map.put(CURRENT_NAMESPACE, namespace == null ? "" : namespace);
        }
        TasksServletRequestCreator.put(message, TasksServletRequestCreator.HEADERS, map);
    }

    protected String getCurrentNamespace() {
        return NamespaceManager.get();
    }

    public String getPath() {
        return taskOptions.getUrl() == null ? getDefaultUrl() : taskOptions.getUrl();
    }

    private String getDefaultUrl() {
        return "/_ah/queue/" + queueName;
    }

    public Class<? extends ServletRequestCreator> getServletRequestCreator() {
        return TasksServletRequestCreator.class;
    }

    private static void setIfNotNull(Message msg, String key, Object value) throws JMSException {
        if (value != null) {
            msg.setStringProperty(key, value.toString());
        }
    }

    private static void putIfNotNull(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }
}
