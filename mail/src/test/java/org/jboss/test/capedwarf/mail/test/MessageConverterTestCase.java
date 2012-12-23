package org.jboss.test.capedwarf.mail.test;

import java.util.Collections;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import com.google.appengine.api.mail.MailService;
import org.jboss.capedwarf.mail.MessageConverter;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class MessageConverterTestCase {

    private static final Session NO_SESSION = null;
    private MailService.Message gaeMessage;

    @Before
    public void setUp() throws Exception {
        gaeMessage = new MailService.Message("sender", "to", "subject", "textBody");
    }

    @Test
    public void senderIsSetCorrectly() throws Exception {
        gaeMessage.setSender("sender@sender.com");
        Message convertedMessage = convertMessage();

        Address[] fromAddresses = convertedMessage.getFrom();
        assertThat(fromAddresses.length, is(1));

        Address fromAddress = fromAddresses[0];
        assertEquals(new InternetAddress("sender@sender.com"), fromAddress);
    }

    @Test
    public void subjectIsConvertedCorrectly() throws Exception {
        gaeMessage.setSubject("Subject");
        Message convertedMessage = convertMessage();
        assertEquals(gaeMessage.getSubject(), convertedMessage.getSubject());
    }

    @Test
    public void recipientIsSetCorrectly() throws Exception {
        gaeMessage.setTo("recipient@recipient.com");
        Message convertedMessage = convertMessage();
        Address[] toRecipients = convertedMessage.getRecipients(Message.RecipientType.TO);
        assertThat(toRecipients.length, is(1));
        assertEquals(new InternetAddress("recipient@recipient.com"), toRecipients[0]);
    }

    @Test
    public void recipientIsSetCorrectlyWhenOverwritten() throws Exception {
        MessageConverter messageConverter = new MessageConverter(gaeMessage, Collections.singleton("admin@admin.com"), NO_SESSION);
        Message convertedMessage = messageConverter.convert();
        Address[] toRecipients = convertedMessage.getRecipients(Message.RecipientType.TO);
        assertThat(toRecipients.length, is(1));
        assertEquals(new InternetAddress("admin@admin.com"), toRecipients[0]);
    }

    private Message convertMessage() throws MessagingException {
        MessageConverter messageConverter = new MessageConverter(gaeMessage, NO_SESSION);
        return messageConverter.convert();
    }
}
