/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.mail;

import com.google.appengine.api.mail.MailService;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossMailService implements MailService {

    private volatile Session session;

    public void send(Message message) throws IOException {
        try {
            send(convertToJavaMail(message));
        } catch (MessagingException e) {
            throw new IOException("Could not send message", e);
        }
    }

    public void sendToAdmins(Message message) throws IOException {
        asssertAllRecipientFieldsAreEmpty(message);
        try {
            send(convertToJavaMail(message, Application.getAdmins()));
        } catch (MessagingException e) {
            throw new IOException("Could not send message", e);
        }
    }

    private void asssertAllRecipientFieldsAreEmpty(Message message) {
        if (!message.getTo().isEmpty()) {
            throw new IllegalArgumentException("to should be empty");
        }
        if (!message.getCc().isEmpty()) {
            throw new IllegalArgumentException("cc should be empty");
        }
        if (!message.getBcc().isEmpty()) {
            throw new IllegalArgumentException("bcc should be empty");
        }
    }

    private Session getSession() {
        if (session == null)
            session = JndiLookupUtils.lookup("mail.jndi.name", Session.class, "java:jboss/mail/Default");

        return session;
    }

    private javax.mail.Message convertToJavaMail(Message message) throws MessagingException {
        MessageConverter converter = new MessageConverter(message, getSession());
        return converter.convert();
    }

    private javax.mail.Message convertToJavaMail(Message message, Collection<String> to) throws MessagingException {
        MessageConverter converter = new MessageConverter(message, to, getSession());
        return converter.convert();
    }

    private void send(javax.mail.Message message) throws MessagingException {
        Transport transport = openTransport();
        try {
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private Transport openTransport() throws MessagingException {
        Transport transport = getSession().getTransport();
        transport.connect();
        return transport;
    }
}
