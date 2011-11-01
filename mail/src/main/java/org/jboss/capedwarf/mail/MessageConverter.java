package org.jboss.capedwarf.mail;

import com.google.appengine.api.mail.MailService;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Collection;

/**
 * Converts GAE Message to JavaMail Message
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class MessageConverter {

    private MailService.Message message;
    private Collection<String> to;
    private Session session;

    public MessageConverter(MailService.Message message, Session session) {
        this(message, null, session);
    }

    public MessageConverter(MailService.Message message, Collection<String> to, Session session) {
        this.message = message;
        this.to = to;
        this.session = session;
    }

    public Message convert() throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setSender(toAddress(message.getSender()));
        msg.addRecipients(Message.RecipientType.TO, toAddressArray(to == null ? message.getTo() : to));
        msg.addRecipients(Message.RecipientType.CC, toAddressArray(message.getCc()));
        msg.addRecipients(Message.RecipientType.BCC, toAddressArray(message.getBcc()));
        msg.setSubject(message.getSubject());

        if (message.getHeaders() != null) {
            for (MailService.Header header : message.getHeaders()) {
                msg.addHeader(header.getName(), header.getValue());
            }
        }

        msg.setContent(createMimeMultiPart());

        return msg;
    }

    private MimeMultipart createMimeMultiPart() throws MessagingException {
        MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.addBodyPart(createHtmlBodyPart());
        mimeMultipart.addBodyPart(createTextBodyPart());

        if (message.getAttachments() != null) {
            for (MailService.Attachment attachment : message.getAttachments()) {
                mimeMultipart.addBodyPart(createAttachmentBodyPart(attachment));
            }
        }

        return mimeMultipart;
    }

    private MimeBodyPart createAttachmentBodyPart(MailService.Attachment attachment) throws MessagingException {
        DataSource source = new ByteArrayDataSource(attachment.getData(), "application/octet-stream");

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(source));
        bodyPart.setFileName(attachment.getFileName());
        return bodyPart;
    }

    private MimeBodyPart createTextBodyPart() throws MessagingException {
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(message.getTextBody());
        return textBodyPart;
    }

    private MimeBodyPart createHtmlBodyPart() throws MessagingException {
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(message.getHtmlBody(), "text/html");
        return htmlBodyPart;
    }

    private static Address[] toAddressArray(Collection<String> addresses) throws AddressException {
        if (addresses == null)
            return null;

        Address[] result = new Address[addresses.size()];
        int i = 0;
        for (String address : addresses) {
            result[i++] = toAddress(address);
        }
        return result;
    }

    private static Address toAddress(String address) throws AddressException {
        return new InternetAddress(address);
    }
}
