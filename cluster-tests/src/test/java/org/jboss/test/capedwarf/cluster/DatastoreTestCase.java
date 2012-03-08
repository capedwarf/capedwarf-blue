package org.jboss.test.capedwarf.cluster;

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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class DatastoreTestCase extends AbstractClusteredTest {

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void putStoresEntityOnDepA() throws Exception {
        System.out.println(">>> putStoresEntityOnDepA");
        Entity entity = createTestEntity("KIND", 1);
        getService().put(entity);
        assertStoreContains(entity);
    }

    @InSequence(31)
    @Test
    @OperateOnDeployment("dep2")
    public void putStoresEntityOnDepB() throws Exception {
        System.out.println(">>> putStoresEntityOnDepB");
        Entity entity = createTestEntity("KIND", 2);
        getService().put(entity);
        assertStoreContains(entity);

        // wait to sync (index is in async mode)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep1")
    public void getEntityOnDepA() throws Exception {
        System.out.println(">>> getEntityOnDepA");
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
        System.out.println(">>> getEntityOnDepB()");
        Entity entity = createTestEntity("KIND", 1);
        assertStoreContains(entity);
    }

    @InSequence(55)
    @Test
    @OperateOnDeployment("dep1")
    public void indexGenInsertOnA() {
        System.out.println(">>> indexGenInsertOnA");
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "A");
        getService().put(entity);

        Entity entity2 = new Entity("indexGen");
        entity2.setProperty("text", "A1");
        getService().put(entity2);

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(2, count);
    }

    @InSequence(60)
    @Test
    @OperateOnDeployment("dep2")
    public void indexGenInsertOnB() {
        // wait to sync (index is in async mode)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(">>> indexGenInsertOnB");
        Entity entity = new Entity("indexGen");
        entity.setProperty("text", "B");
        getService().put(entity);

        // wait to sync (index is in async mode)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int count = getService().prepare(new Query("indexGen")).countEntities(Builder.withDefaults());
        Assert.assertEquals(3, count);
    }

    @InSequence(70)
    @Test
    @OperateOnDeployment("dep1")
    public void indexGenTestOverrideA() {
        System.out.println(">>> indexGenTestOverrideA");

        // wait to sync (index is in async mode)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
    public void indexGenTestOverrideB() {
        System.out.println(">>> indexGenTestOverrideB");
        List<Entity> list = getService().prepare(new Query("indexGen")).asList(Builder.withDefaults());
        Assert.assertEquals(3, list.size());
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

}
