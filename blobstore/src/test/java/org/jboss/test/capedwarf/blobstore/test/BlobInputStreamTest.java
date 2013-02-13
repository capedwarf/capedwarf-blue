package org.jboss.test.capedwarf.blobstore.test;

import java.io.IOException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class BlobInputStreamTest extends BlobstoreTestBase {

    @Deployment
    public static Archive getDeployment() {
        return getCapedwarfDeployment()
            .addClass(BlobstoreTestBase.class)
            .addClass(IOUtils.class);
    }

    @Test
    public void testBlobInputStream() throws Exception {
        String CONTENT = "BlobInputStreamTest";
        BlobKey blobKey = writeNewBlobFile(CONTENT);

        BlobstoreInputStream stream = new BlobstoreInputStream(blobKey);
        assertEquals(CONTENT, toString(stream));
    }

    @Test
    public void testBlobInputStreamWithOffset() throws Exception {
        BlobKey blobKey = writeNewBlobFile("BlobInputStreamTest");

        int OFFSET = 4;
        BlobstoreInputStream stream = new BlobstoreInputStream(blobKey, OFFSET);
        assertEquals("InputStreamTest", toString(stream));
    }

    private String toString(BlobstoreInputStream in) throws IOException {
        byte[] contents = IOUtils.toBytes(in, true);
        return new String(contents);
    }

}
