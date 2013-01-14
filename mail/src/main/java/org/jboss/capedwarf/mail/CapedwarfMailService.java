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

import java.io.IOException;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import com.google.appengine.api.mail.MailService;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfMailService implements MailService {

    private volatile Session session;

    public void send(Message message) throws IOException {
        try {
            send(convertToJavaMail(message));
        } catch (MessagingException e) {
            throw new IOException("Could not send message", e);
        }
    }

    public void sendToAdmins(Message message) throws IOException {
        assertAllRecipientFieldsAreEmpty(message);
        try {
            send(convertToJavaMail(message, getAdminEmails()));
        } catch (MessagingException e) {
            throw new IOException("Could not send message", e);
        }
    }

    private Collection<String> getAdminEmails() {
        return CapedwarfEnvironment.getThreadLocalInstance().getAdmins();
    }

    private void assertAllRecipientFieldsAreEmpty(Message message) {
        if (isNullOrEmpty(message.getTo()) == false) {
            throw new IllegalArgumentException("To should be empty");
        }
        if (isNullOrEmpty(message.getCc()) == false) {
            throw new IllegalArgumentException("Cc should be empty");
        }
        if (isNullOrEmpty(message.getBcc()) == false) {
            throw new IllegalArgumentException("Bcc should be empty");
        }
    }

    private boolean isNullOrEmpty(Collection<String> collection) {
        return (collection == null || collection.isEmpty());
    }

    private Session getSession() {
        if (session == null)
            session = ComponentRegistry.getInstance().getComponent(Keys.MAIL_SESSION);

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
