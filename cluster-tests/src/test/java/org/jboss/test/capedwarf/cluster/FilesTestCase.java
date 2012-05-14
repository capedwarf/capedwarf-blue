package org.jboss.test.capedwarf.cluster;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class FilesTestCase extends AbstractClusteredTest {

    private FileService service;

    @Before
    public void setUp() throws Exception {
        service = FileServiceFactory.getFileService();
    }

    @Test @OperateOnDeployment("dep1") @InSequence(10)
    public void testWriteToNodeA() throws Exception {
        AppEngineFile file = service.createNewBlobFile("text/plain", "single.txt");

        MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
        ms.put(FilesTestCase.class.getSimpleName(), service.getBlobKey(file).getKeyString());

        writeToFileAndFinalize(file, "some-bytes");
        assertEquals("some-bytes", getFileContents(file));
    }

    @Test @OperateOnDeployment("dep2") @InSequence(20)
    public void testReadFromNodeB() throws Exception {
        MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
        String string = (String) ms.get(FilesTestCase.class.getSimpleName());
        BlobKey blobKey = new BlobKey(string);

        AppEngineFile file = service.getBlobFile(blobKey);
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
