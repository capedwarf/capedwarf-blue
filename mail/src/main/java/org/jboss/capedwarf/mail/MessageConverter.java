package org.jboss.capedwarf.mail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.google.appengine.api.mail.MailService;

/**
 * Converts GAE Message to JavaMail Message
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MessageConverter {
    private static final Set<String> INVALID_ATTACHMENT_FILE_TYPES;

    private MailService.Message message;
    private Collection<String> to;
    private Session session;

    static {
        INVALID_ATTACHMENT_FILE_TYPES = getInvalidAttachmentFileTypes();
    }

    protected static Set<String> getInvalidAttachmentFileTypes() {
        String[] extensions = {"ade", "adp", "bat", "chm", "cmd", "com", "cpl", "exe",
                "hta", "ins", "isp", "jse", "lib", "mde", "msc", "msp", "mst", "pif", "scr",
                "sct", "shb", "sys", "vb", "vbe", "vbs", "vxd", "wsc", "wsf", "wsh"};
        return new HashSet<>(Arrays.asList(extensions));
    }

    protected static boolean hasInvalidAttachmentFileType(String fileName) {
        for (String ext : INVALID_ATTACHMENT_FILE_TYPES) {
            if (fileName.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

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

        String htmlBody = message.getHtmlBody();
        if (htmlBody != null) {
            mimeMultipart.addBodyPart(createHtmlBodyPart(htmlBody));
        }

        String textBody = message.getTextBody();
        if (textBody != null) {
            mimeMultipart.addBodyPart(createTextBodyPart(textBody));
        }

        if (message.getAttachments() != null) {
            for (MailService.Attachment attachment : message.getAttachments()) {
                mimeMultipart.addBodyPart(createAttachmentBodyPart(attachment));
            }
        }

        return mimeMultipart;
    }

    private MimeBodyPart createAttachmentBodyPart(MailService.Attachment attachment) throws MessagingException {
        if (hasInvalidAttachmentFileType(attachment.getFileName())) {
            throw new IllegalArgumentException(String.format("Invalid attachment file type: %s", attachment));
        }

        DataSource source = new ByteArrayDataSource(attachment.getData(), "application/octet-stream");

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(source));
        bodyPart.setFileName(attachment.getFileName());
        return bodyPart;
    }

    private MimeBodyPart createTextBodyPart(String body) throws MessagingException {
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(body);
        return textBodyPart;
    }

    private MimeBodyPart createHtmlBodyPart(String body) throws MessagingException {
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(body, "text/html");
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
