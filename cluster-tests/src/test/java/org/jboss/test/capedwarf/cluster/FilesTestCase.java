package org.jboss.test.capedwarf.cluster;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class FilesTestCase extends AbstractClusteredTest {

    private FileService service;

    @Before
    public void setUp() throws Exception {
        service = FileServiceFactory.getFileService();
    }

    @Test @OperateOnDeployment("dep1")
    public void testWriteToNodeA() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "single.txt");
        writeToFileAndFinalize(file, "some-bytes");
        assertEquals("some-bytes", getFileContents(file));
    }

    @Test @OperateOnDeployment("dep2")
    public void testReadFromNodeB() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "single.txt");
        assertEquals("some-bytes", getFileContents(file));
    }


    private void writeToFileAndFinalize(AppEngineFile file, String content) throws IOException {
        writeToFile(file, content, true);
    }

    private void writeToFile(AppEngineFile file, String content, boolean finalize) throws IOException {
        FileWriteChannel channel = service.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(content.getBytes()));
        } finally {
            if (finalize) {
                channel.closeFinally();
            } else {
                channel.close();
            }
        }
    }

    private String getFileContents(AppEngineFile file) throws IOException {
        FileReadChannel channel = service.openReadChannel(file, true);
        try {
            return getStringFromChannel(channel, 1000);
        } finally {
            channel.close();
        }
    }

    private String getStringFromChannel(FileReadChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int bytesRead = channel.read(buffer);

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);

        return new String(bytes);
    }
}
