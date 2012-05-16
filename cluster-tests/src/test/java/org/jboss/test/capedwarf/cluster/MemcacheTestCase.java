package org.jboss.test.capedwarf.cluster;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class MemcacheTestCase extends AbstractClusteredTest {

    protected MemcacheService service;

    @Before
    public void setUp() {
        service = MemcacheServiceFactory.getMemcacheService();
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void testClearAllOnDepA() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");
        service.clearAll();
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
        assertFalse(service.contains("key3"));
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testClearAllOnDepB() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");
        service.clearAll();
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
        assertFalse(service.contains("key3"));
    }

    @Test
    @InSequence(30)
    @OperateOnDeployment("dep1")
    public void testPutOnDepA() {
        service.put("key", "value");
        assertTrue(service.contains("key"));
        assertEquals("value", service.get("key"));
    }

    @Test
    @InSequence(40)
    @OperateOnDeployment("dep2")
    public void testGetOnDepB() {
        //sleep(3000);
        try {
            assertTrue(service.contains("key"));
            assertEquals("value", service.get("key"));
        } finally {
            assertTrue(service.delete("key"));
        }
    }

    @Test
    @InSequence(50)
    @OperateOnDeployment("dep1")
    public void testPutReplaceOnlyIfPresentOnDepA() {
        service.clearAll();
        assertFalse(service.contains("key"));
        service.put("key", "value", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertFalse(service.contains("key"));
        service.put("key", "value");
        service.put("key", "value2", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertEquals("value2", service.get("key"));
    }

    @Test
    @InSequence(60)
    @OperateOnDeployment("dep2")
    public void testPutReplaceOnlyIfPresentOnDepB() {
        try {
            assertEquals("value2", service.get("key"));
            service.put("key", "value3", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
            assertEquals("value3", service.get("key"));
        } finally {
            assertTrue(service.delete("key"));
        }
    }

    @Test
    @InSequence(70)
    @OperateOnDeployment("dep1")
    public void testPutAddOnlyIfNotPresentOnDepA() {
        service.put("key", "firstValue");
        service.put("key", "secondValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        assertEquals("firstValue", service.get("key"));
    }

    @Test
    @InSequence(80)
    @OperateOnDeployment("dep2")
    public void testPutAddOnlyIfNotPresentOnDepB() {
        try {
            service.put("key", "thirdValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
            assertEquals("firstValue", service.get("key"));
        } finally {
            assertTrue(service.delete("key"));
        }
    }

    @Test
    @InSequence(90)
    @OperateOnDeployment("dep1")
    public void testGetIdentifiableOnDepA() {
        service.put("key", "value");
        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");
        assertEquals("value", identifiable.getValue());
    }

    @Test
    @InSequence(100)
    @OperateOnDeployment("dep2")
    public void testGetIdentifiableOnDepB() {
        try {
            MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");
            assertEquals("value", identifiable.getValue());
        } finally {
            assertTrue(service.delete("key"));
        }
    }

//    @Test // TODO
//    @InSequence(110)
//    @OperateOnDeployment("dep1")
    public void testPutIfUntouchedOnDepA() {
        service.put("key", "value");

        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");

        boolean valueWasStored = service.putIfUntouched("key", identifiable, "newValue");
        assertTrue(valueWasStored);
        assertEquals("newValue", service.get("key"));

        boolean valueWasStored2 = service.putIfUntouched("key", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", service.get("key"));
    }

//    @Test // TODO
//    @InSequence(120)
//    @OperateOnDeployment("dep2")
    public void testPutIfUntouchedOnDepB() {
        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");

        boolean valueWasStored2 = service.putIfUntouched("key", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", service.get("key"));
    }

    @Test
    @InSequence(130)
    @OperateOnDeployment("dep1")
    public void testGetAllOnDepA() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");

        Map<String, Object> map = service.getAll(Arrays.asList("key1", "key2"));
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    @InSequence(140)
    @OperateOnDeployment("dep2")
    public void testGetAllOnDepB() {
        List<String> keys = Arrays.asList("key1", "key2");
        try {
            Map<String, Object> map = service.getAll(keys);
            assertEquals(2, map.size());
            assertEquals("value1", map.get("key1"));
            assertEquals("value2", map.get("key2"));
        } finally {
            service.deleteAll(keys);
        }
    }

    @Test
    @InSequence(150)
    @OperateOnDeployment("dep1")
    public void testDeleteOnDepA() {
        service.put("key", "value");
        service.put("key2", "value");
        assertTrue(service.delete("key"));
        assertFalse(service.contains("key"));
    }

    @Test
    @InSequence(160)
    @OperateOnDeployment("dep2")
    public void testDeleteOnDepB() {
        assertTrue(service.contains("key2"));
        assertTrue(service.delete("key2"));
        assertFalse(service.contains("key2"));
    }

    @Test
    @InSequence(170)
    @OperateOnDeployment("dep1")
    public void testDeleteAllOnDepA() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");
        service.put("key4", "value4");
        service.put("key5", "value5");
        service.deleteAll(Arrays.asList("key1", "key2"));
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
        assertTrue(service.contains("key3"));
        assertTrue(service.contains("key4"));
    }

    @Test
    @InSequence(180)
    @OperateOnDeployment("dep2")
    public void testDeleteAllOnDepB() {
        try {
            assertTrue(service.contains("key3"));
            assertTrue(service.contains("key4"));
            assertTrue(service.contains("key5"));

            service.deleteAll(Arrays.asList("key3", "key4"));

            assertFalse(service.contains("key3"));
            assertFalse(service.contains("key4"));
            assertTrue(service.contains("key5"));
        } finally {
            assertTrue(service.delete("key5"));
        }
    }

    @Test
    @InSequence(190)
    @OperateOnDeployment("dep1")
    public void testPutExpirationOnDepA() {
        service.put("key", "value", Expiration.byDeltaMillis(3000));
        assertTrue(service.contains("key"));
    }

    @Test
    @InSequence(200)
    @OperateOnDeployment("dep2")
    public void testPutExpirationOnDepB() {
        assertTrue(service.contains("key"));
        sleep(4000);
        assertFalse(service.contains("key"));
    }

    @Test
    @InSequence(200)
    @OperateOnDeployment("dep1")
    public void testIncrementOnDepA() {
        long x = service.increment("increment-key", 5, 0L);
        assertEquals(0L, x);
        x = service.increment("increment-key", 15);
        assertEquals(15L, x);
        x = service.increment("increment-key", 6);
        assertEquals(21L, x);
    }

    @Test
    @InSequence(210)
    @OperateOnDeployment("dep2")
    public void testIncrementOnDepB() {
        try {
            long x = service.increment("increment-key", 4);
            assertEquals(25L, x);
        } finally {
            assertTrue(service.delete("increment-key"));
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @InSequence(1000)
    @OperateOnDeployment("dep1")
    public void tearDownDepA() {
        tearDown();
    }

    @Test
    @InSequence(1000)
    @OperateOnDeployment("dep2")
    public void tearDownDepB() {
        tearDown();
    }

    private void tearDown() {
        service.clearAll();
    }


}
