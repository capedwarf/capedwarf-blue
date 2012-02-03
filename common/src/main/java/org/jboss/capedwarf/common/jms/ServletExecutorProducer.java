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

package org.jboss.capedwarf.common.jms;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;
import org.jboss.modules.ModuleClassLoader;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;


/**
 * JMS producer for servlet executor.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServletExecutorProducer {

    private static final ServletExecutorProducer instance = new ServletExecutorProducer();
    private static final String PREFIX = "org_jboss_capedwarf_jms_";

    private volatile int counter;

    private volatile Connection connection;
    private Session session;
    private volatile MessageProducer producer;

    public static ServletExecutorProducer getInstance() {
        return instance;
    }

    private MessageProducer getProducer() throws Exception {
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    final ConnectionFactory factory = JndiLookupUtils.lookup("jms.factory.jndi", ConnectionFactory.class, "java:/JmsXA");
                    final Connection tmp = factory.createConnection();
                    session = tmp.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    final Queue queue = JndiLookupUtils.lookup("jms.queue.jndi", Queue.class, "java:/queue/capedwarf");
                    producer = session.createProducer(queue);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    connection = tmp;
                }
            }
        }
        return producer;
    }

    public void start() {
        counter++;
    }

    public void stop() throws Exception {
        counter--;
        if (counter == 0) {
            final Connection tmp = connection;
            connection = null;
            if (tmp != null)
                tmp.close();
        }
    }

    /**
     * Send jms message.
     *
     * @param creator the message creator
     * @return msg id
     * @throws Exception for any error
     */
    public String sendMessage(MessageCreator creator) throws Exception {
        final MessageProducer mp = getProducer();

        Message message;
        synchronized (this) {
            message = creator.createMessage(session);
            if (message == null) {
                message = session.createMessage();
                creator.enhanceMessage(message);
            }
        }

        message.setStringProperty(PREFIX + "module", getModuleName());
        message.setStringProperty(PREFIX + "appId", Application.getAppId());
        message.setStringProperty(PREFIX + "path", creator.getPath());
        message.setStringProperty(PREFIX + "factory", creator.getServletRequestCreator().getName());

        synchronized (this) {
            mp.send(message);
        }

        return message.getJMSMessageID();
    }

    private static String getModuleName() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl instanceof ModuleClassLoader == false) {
            cl = cl.getParent();
        }
        if (cl == null)
            throw new IllegalArgumentException("No ModuleClassLoader found in hierarchy.");

        final ModuleClassLoader mcl = (ModuleClassLoader) cl;
        return mcl.getModule().getIdentifier().toString();
    }
}
