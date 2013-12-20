/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.config.QueueXml;
import org.wildfly.mail.ra.MailListener;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "mailServer", propertyValue = ""),
        @ActivationConfigProperty(propertyName = "userName", propertyValue = ""),
        @ActivationConfigProperty(propertyName = "password", propertyValue = ""),
        @ActivationConfigProperty(propertyName = "storeProtocol", propertyValue = "imaps"),
        @ActivationConfigProperty(propertyName = "mailFolder", propertyValue = ""),
        @ActivationConfigProperty(propertyName = "pollingInterval", propertyValue = "5000")
    })
public class CapedwarfInboundMailMDB implements MailListener {


    @Override
    public void onMessage(Message msg) {
        CapedwarfEnvironment.createThreadLocalInstance();
        try {
            deliverMessage(msg);
        } finally {
            CapedwarfEnvironment.clearThreadLocalInstance();
        }
    }

    public void deliverMessage(final Message message) {
        try {
            Queue queue = QueueFactory.getQueue(QueueXml.INTERNAL);
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            for (Address recipient : recipients) {
                if (isLocalRecipient(recipient)) {
                    queue.add(
                        withUrl(getUrl(recipient))
                            .payload(toBytes(message), message.getContentType()));
                }
            }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrl(Address recipient) {
        return "/_ah/mail/" + recipient;
    }

    private byte[] toBytes(Message message) throws IOException, MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        message.writeTo(baos);
        return baos.toByteArray();
    }

    private boolean isLocalRecipient(Address address) {
        return true; // TODO
    }

}
