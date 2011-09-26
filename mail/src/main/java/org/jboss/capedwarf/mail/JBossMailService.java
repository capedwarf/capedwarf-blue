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
import org.jboss.capedwarf.common.jndi.JndiLookupUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.IOException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossMailService implements MailService {

    private Session session;

    public void send(Message message) throws IOException {
        try {
            initSessionIfNeeded();
            send(convertToJavaMail(message));
        } catch (MessagingException e) {
            throw new IOException("Could not send message", e);
        }
    }

    private void initSessionIfNeeded() {
        if (session == null) {
             session = JndiLookupUtils.lookup("mail.jndi.name", Session.class, "java:jboss/mail/Default");
        }
    }

    public void sendToAdmins(Message message) throws IOException {
        // TODO
    }

    private javax.mail.Message convertToJavaMail(Message message) throws MessagingException {
        MessageConverter converter = new MessageConverter(message, session);
        return converter.convert();
    }

    private void send(javax.mail.Message javaMailMessage) throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect();
        try {
            transport.sendMessage(javaMailMessage, javaMailMessage.getAllRecipients());
        } finally {
            transport.close();
        }
    }
}
