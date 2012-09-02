package org.jboss.test.capedwarf.cluster;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.capedwarf.datastore.JBossDatastoreService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class DatastoreTestCase extends AbstractClusteredTest {

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void putStoresEntityOnDepA() throws Exception {
        Entity entity = createTestEntity("KIND", 1);
        getService().put(entity);
        assertStoreContains(entity);
    }

    @InSequence(31)
    @Test
    @OperateOnDeployment("dep2")
    public void putStoresEntityOnDepB() throws Exception {
        Entity entity = createTestEntity("KIND", 2);
        getService().put(entity);
        assertStoreContains(entity);
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep1")
    public void getEntityOnDepA() throws Exception {
        waitForSync();

        Key key = KeyFactory.createKey("KIND", 1);
        Entity lookup = getService().get(key);

        Assert.assertNotNull(lookup);

        Entity entity = createTestEntity("KIND", 1);
        Assert.assertEquals(entity, lookup);
    }

    @InSequence(50)
    @Test
    @OperateOnDeployment("dep2")
    public void getEntityOnDepB() throws Exception {
        waitForSync();

        Entity entity = createTestEntity("KIND", 1);
        assertStoreContains(entity);
    }

    @InSequence(52)
    @Test
    @OperateOnDeployment("dep1")
    public void queryOnA() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("KIND")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(53)
    @Test
    @OperateOnDeployment("dep2")
    public void queryOnB() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("KIND")).countEntities(Builder.withDefaults());
        Assert.assertTrue("Number of entities: " + count, count == 2);
    }

    @InSequence(55)
    @Test
    @OperateOnDeployment("dep1")
    public void indexGenAndQueryInsertOnA() throws Exception {
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "A");
        Key key = getService().put(entity);

        Assert.assertNotNull(getService().get(KeyFactory.createKey("indexGen", key.getId())));

        Entity entity2 = new Entity("indexGen");
        entity2.setProperty("text", "A1");
        getService().put(entity2);

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(2, count);
    }

    @InSequence(60)
    @Test
    @OperateOnDeployment("dep2")
    public void indexGenAndQueryInsertOnB() throws Exception {
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "B");
        getService().put(entity);

        waitForSync();

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(3, count);
    }

    @InSequence(70)
    @Test
    @OperateOnDeployment("dep1")
    public void testContentOnA() throws Exception {
        waitForSync();

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(3, count);

        List<Entity> list = getService().prepare(new Query("indexGen")).asList(Builder.withDefaults());
        Assert.assertEquals(3, list.size());

        List<String> cached = new ArrayList<String>();
        for (Entity entity : list) {
            cached.add((String) entity.getProperty("text"));
        }
        Assert.assertTrue(cached.contains("A"));
        Assert.assertTrue(cached.contains("A1"));
        Assert.assertTrue(cached.contains("B"));
    }

    @InSequence(80)
    @Test
    @OperateOnDeployment("dep2")
    public void testContentOnB() throws Exception {
        waitForSync();

        List<Entity> list = getService().prepare(new Query("indexGen")).asList(Builder.withDefaults());
        Assert.assertEquals(3, list.size());

        List<String> cached = new ArrayList<String>();
        for (Entity entity : list) {
            cached.add((String) entity.getProperty("text"));
        }

        Assert.assertTrue(cached.contains("A"));
        Assert.assertTrue(cached.contains("A1"));
        Assert.assertTrue(cached.contains("B"));
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void tearDownDepA() throws Exception {
        tearDown();
    }

    @InSequence(1010)
    @Test
    @OperateOnDeployment("dep2")
    public void tearDownDepB() throws Exception {
        tearDown();
    }

    private DatastoreService getService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    private void tearDown() {
        ((JBossDatastoreService) getService()).clearCache();
    }

    private Entity createTestEntity(String kind, int id) {
        Key key = KeyFactory.createKey(kind, id);
        Entity entity = new Entity(key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    private void assertStoreContains(Entity entity) throws EntityNotFoundException {
        Entity lookup = getService().get(entity.getKey());
        Assert.assertNotNull(lookup);
        Assert.assertEquals(entity, lookup);
    }

    private void waitForSync() throws InterruptedException {
        Thread.sleep(5000L);
    }

}
