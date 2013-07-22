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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.NamespaceManager;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.jms.ServletExecutorProducer;
import org.jboss.capedwarf.common.servlet.AbstractHttpServletRequest;
import org.jboss.capedwarf.shared.config.QueueXml;
import org.jboss.capedwarf.shared.jms.AbstractServletRequestCreator;
import org.jboss.capedwarf.shared.jms.MessageConstants;

/**
 * Tasks servlet request creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class TasksServletRequestCreator extends AbstractServletRequestCreator {

    private static final String COPY = "_copy";

    static final String DELIMITER = "||";
    static final String METHOD = "task_option_method";
    static final String HEADERS = "task_option_headers_";
    static final String PARAMS = "task_option_params_";

    private static final String JMSX_DELIVERY_COUNT = "JMSXDeliveryCount";
    private static final String REGEX_SAFE_DELIMITER = Pattern.quote(DELIMITER);

    public HttpServletRequest createServletRequest(ServletContext context, Message message) throws Exception {
        AbstractHttpServletRequest request;
        if (message instanceof BytesMessage) {
            request = new BytesServletRequest(context, (BytesMessage) message);
        } else {
            request = new TasksServletRequest(context);
        }
        String path = ServletExecutorProducer.getString(message, MessageConstants.PATH);
        request.setServletPath(path);
        request.setMethod(message.getStringProperty(METHOD));
        request.addHeaders(get(message, HEADERS, false));
        request.addParameters(get(message, PARAMS, true));

        int deliveryCount = message.getIntProperty(JMSX_DELIVERY_COUNT);
        String executionCount = String.valueOf(deliveryCount - 1);
        request.addHeader(TasksMessageCreator.TASK_EXECUTION_COUNT, executionCount);
        request.addHeader(TasksMessageCreator.TASK_RETRY_COUNT, executionCount);

        Object eta = request.getHeader(TasksMessageCreator.TASK_ETA);
        if (eta == null) {
            request.addHeader(TasksMessageCreator.TASK_ETA, String.valueOf(System.currentTimeMillis()));
        }

        return request;
    }

    public void prepare(HttpServletRequest request, String appId) {
        CapedwarfEnvironment.createThreadLocalInstance();
        String namespace = request.getHeader(TasksMessageCreator.CURRENT_NAMESPACE);
        NamespaceManager.set(namespace);
    }

    public void finish() {
        CapedwarfEnvironment.clearThreadLocalInstance();
    }

    @Override
    public boolean isValid(HttpServletRequest request, HttpServletResponse response) {
        boolean result = super.isValid(request, response);
        if (result == false) {
            String queueName = request.getHeader(TasksMessageCreator.QUEUE_NAME_HEADER);
            result = QueueXml.INTERNAL.equals(queueName) && isStatusInRange(response, 403, 404);
        }
        return result;
    }

    private static class TasksServletRequest extends AbstractHttpServletRequest {
        private TasksServletRequest(ServletContext context) {
            super(context);
        }
    }

    private static class BytesServletRequest extends AbstractHttpServletRequest {
        private final BytesMessage msg;
        private final byte[] buf = new byte[1];

        private BytesServletRequest(ServletContext context, BytesMessage msg) {
            super(context);
            this.msg = msg;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                public int read() throws IOException {
                    try {
                        final int rc = msg.readBytes(buf, 1);
                        return (rc != -1) ? (buf[0] & 0xFF) : -1;
                    } catch (JMSException e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public void close() throws IOException {
                    reset(); // reset on close, so it can be reused
                }

                @Override
                public synchronized void reset() throws IOException {
                    try {
                        msg.reset();
                    } catch (JMSException e) {
                        throw new IOException(e);
                    }
                }

                public boolean isFinished() {
                    return false; // TODO
                }

                public boolean isReady() {
                    return false; // TODO
                }

                public void setReadListener(ReadListener readListener) {
                }
            };
        }
    }

    static void put(final Message msg, final String prefix, final Map<String, String> map) throws JMSException {
        int offset = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            put(msg, prefix, entry.getKey(), entry.getValue(), offset++);
        }
    }

    private static void put(final Message msg, final String prefix, final String key, final String value, final int offset) throws JMSException {
        msg.setStringProperty(prefix + offset, key);
        msg.setStringProperty(prefix + offset + COPY, value);
    }

    static Map<String, Set<String>> get(Message msg, String prefix, boolean urlDecode) throws JMSException {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        Enumeration names = msg.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            String value = msg.getStringProperty(name);
            if (name.startsWith(prefix) && name.endsWith(COPY) == false) {
                String property = msg.getStringProperty(name + COPY);
                if (property != null) {
                    String[] values = property.split(REGEX_SAFE_DELIMITER);
                    if (urlDecode) {
                        urlDecode(values);
                    }
                    map.put(value, new LinkedHashSet<String>(Arrays.asList(values)));
                }
            }
        }
        return map;
    }

    private static void urlDecode(String[] values) throws JMSException {
        try {
            for (int i = 0; i < values.length; i++) {
                values[i] = URLDecoder.decode(values[i], "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new JMSException("UTF-8 not supported on this platform");
        }
    }
}
