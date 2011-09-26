package org.jboss.test.capedwarf.mail.test;

import com.google.appengine.api.mail.MailService;
import org.jboss.capedwarf.mail.MessageConverter;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.Message;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class MessageConverterTestCase {

    @Test
    public void subjectIsConvertedCorrectly() throws Exception {
        MailService.Message gaeMessage = new MailService.Message();
        gaeMessage.setSubject("Subject");
        MessageConverter messageConverter = new MessageConverter(gaeMessage, null);

        Message convertedMessage = messageConverter.convert();
        Assert.assertEquals(gaeMessage.getSubject(), convertedMessage.getSubject());
    }

    // TODO
}
