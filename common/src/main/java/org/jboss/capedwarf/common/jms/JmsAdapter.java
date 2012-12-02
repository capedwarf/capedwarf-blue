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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.jboss.capedwarf.common.jndi.JndiLookupUtils;


/**
 * Expose JMS resources.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class JmsAdapter {
    private Session session;
    private MessageProducer producer;
    private Connection connection;

    protected Session getSession() throws Exception {
        if (session == null) {
            final ConnectionFactory factory = JndiLookupUtils.lookup("jms.factory.jndi", ConnectionFactory.class, "java:/JmsXA");
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        return session;
    }

    protected Queue getQueue() throws Exception {
        return JndiLookupUtils.lookup("jms.queue.jndi", Queue.class, "java:/queue/capedwarf");
    }

    protected QueueBrowser getBrowser() throws Exception {
        return getSession().createBrowser(getQueue());
    }

    protected MessageProducer getProducer() throws Exception {
        if (producer == null) {
            final Queue queue = getQueue();
            producer = getSession().createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }
        return producer;
    }

    public void dispose() {
        final Connection tmp = connection;
        connection = null;
        if (tmp != null) {
            try {
                tmp.close();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
