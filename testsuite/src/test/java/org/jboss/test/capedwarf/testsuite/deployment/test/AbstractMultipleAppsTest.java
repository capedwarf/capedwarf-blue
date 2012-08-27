package org.jboss.test.capedwarf.testsuite.deployment.test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import junit.framework.Assert;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractMultipleAppsTest {
    protected static WebArchive getDeployment(String suffix) {
        return ShrinkWrap.create(WebArchive.class, "test-" + suffix + ".war")
                .addClass(AbstractMultipleAppsTest.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web-" + suffix + ".xml", "appengine-web.xml");
    }

    protected void allTests() throws Exception {
        testEmptyDS();
        testDSTouch();
    }

    protected void testEmptyDS() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("MKind");
        PreparedQuery pq = ds.prepare(query);
        Assert.assertFalse(pq.asIterator().hasNext());
    }

    protected void testDSTouch() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key key = ds.put(new Entity("MKind"));
        Assert.assertTrue(ds.get(key) != null);
    }
}
