package org.jboss.test.capedwarf.testsuite.socket;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class SocketTest extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment(TestContext.withBlackList());
    }

    @Test
    public void testOutboundSocket() throws Exception {
        Socket socket = new Socket("whois.internic.net", 43);
        try {
            socket.setSoTimeout(10000);
            Writer out = new OutputStreamWriter(socket.getOutputStream(), "8859_1");
            Reader in = new InputStreamReader(socket.getInputStream(), "8859_1");

            out.write("=" + "google.com" + "\r\n");
            out.flush();

            StringBuilder builder = new StringBuilder();
            for (int c; (c = in.read()) != -1;) {
                builder.append((char) c);
            }

            assertTrue(builder.length() > 0);
        } finally {
            socket.close();
        }
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testInboundSocket() throws Exception {
        new ServerSocket(4123);
    }
}
