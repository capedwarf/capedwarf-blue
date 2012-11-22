package org.jboss.test.capedwarf.cluster.test;

import java.nio.ByteBuffer;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class BlobstoreTestCase extends AbstractClusteredTest {

    private static final String TEXT = "Uploaded text";

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void insertIntoBlobstoreOnDep1() throws Exception {
        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();

        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("text/plain", "uploadedText.txt");
        FileWriteChannel channel = fileService.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(TEXT.getBytes()));
        } finally {
            channel.closeFinally();
        }

        waitForSync();
        BlobKey blobKey = fileService.getBlobKey(file);
        System.out.println("Blob key: " + blobKey);
        byte[] bytes = service.fetchData(blobKey, 0, Long.MAX_VALUE);

        Assert.assertEquals(TEXT, new String(bytes));

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity dsBK = new Entity("blobTestId", 1);
        dsBK.setProperty("blogId", blobKey.getKeyString());
        ds.put(dsBK);
        waitForSync();
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void readFromBlobstoreOnDep2() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("blobTestId", 1);
        Entity dsBK = ds.get(key);
        String blobKeyStr = (String) dsBK.getProperty("blogId");
        BlobKey blobKey = new BlobKey(blobKeyStr);
        if (blobKeyStr == null || blobKeyStr.equals("")) {
            fail("Datastore should have this value. Try to run clustered DatastoreTestCase first.");
        }

        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();
        System.out.println("Blob key: " + blobKey);
        byte[] bytes = service.fetchData(blobKey, 0, Long.MAX_VALUE);
        try {
            Assert.assertEquals(TEXT, new String(bytes));
        } finally {
            service.delete(blobKey);
        }
    }

    private void waitForSync() {
        sync();
    }
}
