/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.capedwarf.common.util.Util;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class QueueUtils {
    private static final ObjectName QUEUE_NAME;

    private static final String COUNT_MESSAGES = "countMessages";
    private static final String[] COUNT_MESSAGES_SIGNATURE = new String[]{String.class.getName()};
    private static final String LIST_MESSAGES = "listMessages";
    private static final String[] LIST_MESSAGES_SIGNATURE = new String[]{String.class.getName()};
    private static final String LIST_SCHEDULED_MESSAGES = "listScheduledMessages";
    private static final String[] EMPTY_SIGNATURE = new String[0];
    private static final String REMOVE_MESSAGE = "removeMessage";
    private static final String[] REMOVE_MESSAGE_SIGNATURE = new String[]{Long.TYPE.getName()};
    private static final String REMOVE_MESSAGES = "removeMessages";
    private static final String[] REMOVE_MESSAGES_SIGNATURE = new String[]{String.class.getName()};

    static {
        try {
            QUEUE_NAME = ObjectName.getInstance("org.hornetq:module=Core,type=Queue,address=\"jms.queue.capedwarfQueue\",name=\"jms.queue.capedwarfQueue\"");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static MBeanServer getMBeanServer() {
        List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        if (servers.isEmpty()) {
            throw new IllegalStateException("Cannot check for duplicate task names, no MBeanServer found!");
        }
        return servers.get(0);
    }

    private static <T> T invoke(MBeanServer server, Class<T> expectedType, String method, Object[] args, String[] sig) {
        try {
            return expectedType.cast(server.invoke(QUEUE_NAME, method, args, sig));
        } catch (Exception e) {
            throw Util.toRuntimeException(e);
        }
    }

    private static String toFilter(String key, String value) {
        return key + "='" + value + "'";
    }

    private static long count(MBeanServer server, String queueName, String taskName) {
        String filter = (taskName != null) ?
                toFilter(TasksMessageCreator.QUEUE_NAME_KEY, queueName) + " AND " + toFilter(TasksMessageCreator.TASK_NAME_KEY, taskName) :
                toFilter(TasksMessageCreator.QUEUE_NAME_KEY, queueName);

        Object[] args = {filter};
        return invoke(server, Long.class, COUNT_MESSAGES, args, COUNT_MESSAGES_SIGNATURE);
    }

    private static Map<String, Object>[] current(MBeanServer server, String queueName, String taskName) {
        String filter = (taskName != null) ?
                toFilter(TasksMessageCreator.QUEUE_NAME_KEY, queueName) + " AND " + toFilter(TasksMessageCreator.TASK_NAME_KEY, taskName) :
                toFilter(TasksMessageCreator.QUEUE_NAME_KEY, queueName);

        Object[] args = {filter};
        return list(server, LIST_MESSAGES, args, LIST_MESSAGES_SIGNATURE);
    }

    private static List<Map<String, Object>> scheduled(MBeanServer server, String queueName, String taskName) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        Map<String, Object>[] scheduledMsgs = list(server, LIST_SCHEDULED_MESSAGES, new Object[0], EMPTY_SIGNATURE);
        for (Map<String, Object> msg : scheduledMsgs) {
            String qName = msg.get(TasksMessageCreator.QUEUE_NAME_KEY).toString();
            if (queueName.equals(qName) && (taskName == null || taskName.equals(msg.get(TasksMessageCreator.TASK_NAME_KEY).toString()))) {
                results.add(msg);
            }
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object>[] list(MBeanServer server, String method, Object[] args, String[] sig) {
        return invoke(server, Map[].class, method, args, sig);
    }

    static boolean removeMessage(long messageID) {
        return invoke(getMBeanServer(), Boolean.class, REMOVE_MESSAGE, new Object[]{messageID}, REMOVE_MESSAGE_SIGNATURE);
    }

    static boolean removeMessage(String taskName) {
        return invoke(getMBeanServer(), Boolean.class, REMOVE_MESSAGES, new Object[]{toFilter(TasksMessageCreator.TASK_NAME_KEY, taskName)}, REMOVE_MESSAGES_SIGNATURE);
    }

    static List<Map<String, Object>> list(String queueName) {
        return list(queueName, null);
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> list(String queueName, String taskName) {
        if (queueName == null) {
            throw new IllegalArgumentException("Null queue name!");
        }

        MBeanServer server = getMBeanServer();

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        Map<String, Object>[] currentMsgs = current(server, queueName, taskName);
        results.addAll(Arrays.asList(currentMsgs));

        results.addAll(scheduled(server, queueName, taskName));

        return results;
    }

    static long count(String queueName) {
        return count(queueName, null);
    }

    static long count(String queueName, String taskName) {
        return count(getMBeanServer(), queueName, taskName);
    }

    static Map<String, Object>[] current(String queueName) {
        return current(queueName, null);
    }

    static Map<String, Object>[] current(String queueName, String taskName) {
        return current(getMBeanServer(), queueName, taskName);
    }

    static List<Map<String, Object>> scheduled(String queueName) {
        return scheduled(queueName, null);
    }

    static List<Map<String, Object>> scheduled(String queueName, String taskName) {
        return scheduled(getMBeanServer(), queueName, taskName);
    }
}
