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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.SimpleAppIdFactory;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.InboundServices;
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

    public static final String BOUNCE_URL = "/_ah/bounce";
    public static final String MAIL_URL_PREFIX = "/_ah/mail/";

    public static final String X_FAILED_RECIPIENTS = "X-failed-recipients";

    @Override
    public void onMessage(Message msg) {
        AppIdFactory.setCurrentFactory(SimpleAppIdFactory.getInstance());
        try {
            CapedwarfEnvironment.createThreadLocalInstance();
            try {
                deliverMessage(msg);
            } finally {
                CapedwarfEnvironment.clearThreadLocalInstance();
            }
        } finally {
            AppIdFactory.resetCurrentFactory();
        }
    }

    protected void deliverMessage(final Message message) {
        try {
            if (isBounce(message)) {
                if (isEnabled(InboundServices.Service.mail_bounce)) {
                    deliverBounceNotification(message);
                }
            } else {
                if (isEnabled(InboundServices.Service.mail)) {
                    deliverRegularMessage(message);
                }
            }
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deliverBounceNotification(final Message message) throws IOException, MessagingException {
        MyMimeMultipart mimeMultipart = new MyMimeMultipart();

        MimeBodyPart rawMessagePart = new MimeBodyPart();
        rawMessagePart.setHeader("Content-Disposition", "form-data; name=raw-message");
        rawMessagePart.setHeader("Content-Type", "text/plain");
        rawMessagePart.setDataHandler(new DataHandler(new RawMessageDataSource(message)));
        mimeMultipart.addBodyPart(rawMessagePart);

        mimeMultipart.addBodyPart(createFormDataPart("notification-from", message.getFrom()[0].toString()));
        mimeMultipart.addBodyPart(createFormDataPart("notification-to", message.getRecipients(Message.RecipientType.TO)[0].toString()));
        mimeMultipart.addBodyPart(createFormDataPart("notification-subject", message.getSubject()));
        mimeMultipart.addBodyPart(createFormDataPart("notification-text", String.valueOf(message.getContent())));

        mimeMultipart.addBodyPart(createFormDataPart("original-from", "TODO"));
        mimeMultipart.addBodyPart(createFormDataPart("original-to", "TODO"));
        mimeMultipart.addBodyPart(createFormDataPart("original-subject", "TODO"));
        mimeMultipart.addBodyPart(createFormDataPart("original-text", "TODO"));

        mimeMultipart.updateHeaders();

        Queue queue = QueueFactory.getQueue(QueueXml.INTERNAL);
        queue.add(withUrl(BOUNCE_URL).payload(toBytes(mimeMultipart), mimeMultipart.getContentType()));
    }

    private MimeBodyPart createFormDataPart(String name, String content) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setHeader("Content-Disposition", "form-data; name=" + name);
        part.setHeader("Content-Type", "text/plain");
        part.setContent(content, "text/plain");
        return part;
    }

    private void deliverRegularMessage(Message message) throws MessagingException, IOException {
        Queue queue = QueueFactory.getQueue(QueueXml.INTERNAL);
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        for (Address recipient : recipients) {
            if (isLocalRecipient(recipient)) {
                queue.add(withUrl(MAIL_URL_PREFIX + recipient).payload(toBytes(message), message.getContentType()));
            }
        }
    }

    private boolean isBounce(Message message) throws MessagingException {
        return message.getHeader(X_FAILED_RECIPIENTS) != null;
    }

    private boolean isEnabled(InboundServices.Service service) {
        return ApplicationConfiguration.getInstance().getAppEngineWebXml().isInboundServiceEnabled(service);
    }

    private byte[] toBytes(Multipart multipart) throws IOException, MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        multipart.writeTo(baos);
        return baos.toByteArray();
    }

    private byte[] toBytes(Part message) throws IOException, MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        message.writeTo(baos);
        return baos.toByteArray();
    }

    private boolean isLocalRecipient(Address address) {
        return true; // TODO
    }

    private static class MyMimeMultipart extends MimeMultipart {
        @Override
        public void updateHeaders() throws MessagingException {
            super.updateHeaders();
        }
    }

    private class RawMessageDataSource implements DataSource {
        private final Message message;

        public RawMessageDataSource(Message message) {
            this.message = message;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return new ByteArrayInputStream(toBytes(message));
            } catch (MessagingException e) {
                throw new IOException(e);
            }
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        @Override
        public String getContentType() {
            return "text/plain";
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
