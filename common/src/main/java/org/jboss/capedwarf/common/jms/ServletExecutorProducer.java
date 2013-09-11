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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.shared.jms.MessageConstants;
import org.jboss.capedwarf.shared.util.Utils;
import org.jboss.modules.Module;


/**
 * JMS producer for servlet executor.
 * This producer is not thread safe!
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServletExecutorProducer extends JmsAdapter {
    /**
     * Send jms message.
     *
     * @param creator the message creator
     * @return msg id
     * @throws Exception for any error
     */
    public String sendMessage(MessageCreator creator) throws Exception {
        final MessageProducer mp = getProducer();

        Message message = creator.createMessage(getSession());
        if (message == null) {
            message = getSession().createMessage();
            creator.enhanceMessage(message);
        }

        setString(message, MessageConstants.MODULE, getModuleName());
        setString(message, MessageConstants.APP_ID, Application.getAppId());
        setString(message, MessageConstants.PATH, creator.getPath());
        setString(message, MessageConstants.FACTORY, creator.getServletRequestCreator().getName());

        mp.send(message);

        return message.getJMSMessageID();
    }

    private static void setString(final Message msg, String key, String value) throws JMSException {
        msg.setStringProperty(MessageConstants.PREFIX + key, value);
    }

    private static String getModuleName() {
        final Module module = Utils.toModule();
        return module.getIdentifier().toString();
    }
}
