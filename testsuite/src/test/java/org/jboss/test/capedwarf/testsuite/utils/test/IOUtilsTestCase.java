package org.jboss.test.capedwarf.testsuite.utils.test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;

import junit.framework.Assert;
import org.jboss.capedwarf.common.io.IOUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class IOUtilsTestCase {

    @Test
    public void testSkipSkipsOverCorrectNumberOfBytes() throws Exception{
        InputStream stream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5});
        IOUtils.skipFully(stream, 2);
        Assert.assertEquals(2, stream.read());
    }

    @Test(expected = EOFException.class)
    public void testSkipThrowsEOFWhenSkippingOverWholeStream() throws Exception{
        IOUtils.skipFully(new ByteArrayInputStream(new byte[10]), 20);
    }
}
