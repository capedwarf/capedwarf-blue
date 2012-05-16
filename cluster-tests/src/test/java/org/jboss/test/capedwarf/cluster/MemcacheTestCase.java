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
 * @author Ales Justin
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
        service.put("key01", "value1");
        service.put("key02", "value2");
        service.put("key03", "value3");
        service.clearAll();
        assertFalse(service.contains("key01"));
        assertFalse(service.contains("key02"));
        assertFalse(service.contains("key03"));
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testClearAllOnDepB() {
        sleep(3000);
        service.put("key01", "value1");
        service.put("key02", "value2");
        service.put("key03", "value3");
        service.clearAll();
        assertFalse(service.contains("key01"));
        assertFalse(service.contains("key02"));
        assertFalse(service.contains("key03"));
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
        sleep(3000);
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
        assertFalse(service.contains("key-replace-only"));
        service.put("key-replace-only", "value", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertFalse(service.contains("key-replace-only"));
        service.put("key-replace-only", "value");
        service.put("key-replace-only", "value2", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertEquals("value2", service.get("key-replace-only"));
    }

    @Test
    @InSequence(60)
    @OperateOnDeployment("dep2")
    public void testPutReplaceOnlyIfPresentOnDepB() {
        sleep(3000);
        try {
            assertEquals("value2", service.get("key-replace-only"));
            service.put("key-replace-only", "value3", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
            assertEquals("value3", service.get("key-replace-only"));
        } finally {
            assertTrue(service.delete("key-replace-only"));
        }
    }

    @Test
    @InSequence(70)
    @OperateOnDeployment("dep1")
    public void testPutAddOnlyIfNotPresentOnDepA() {
        service.put("key-only-present", "firstValue");
        service.put("key-only-present", "secondValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        assertEquals("firstValue", service.get("key-only-present"));
    }

    @Test
    @InSequence(80)
    @OperateOnDeployment("dep2")
    public void testPutAddOnlyIfNotPresentOnDepB() {
        sleep(3000);
        try {
            service.put("key-only-present", "thirdValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
            assertEquals("firstValue", service.get("key-only-present"));
        } finally {
            assertTrue(service.delete("key-only-present"));
        }
    }

    @Test
    @InSequence(90)
    @OperateOnDeployment("dep1")
    public void testGetIdentifiableOnDepA() {
        service.put("key-identifiable", "value");
        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key-identifiable");
        assertEquals("value", identifiable.getValue());
    }

    @Test
    @InSequence(100)
    @OperateOnDeployment("dep2")
    public void testGetIdentifiableOnDepB() {
        sleep(3000);
        try {
            MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key-identifiable");
            assertEquals("value", identifiable.getValue());
        } finally {
            assertTrue(service.delete("key-identifiable"));
        }
    }

//    @Test // TODO
//    @InSequence(110)
//    @OperateOnDeployment("dep1")
    public void testPutIfUntouchedOnDepA() {
        service.put("key-untouched", "value");

        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key-untouched");

        boolean valueWasStored = service.putIfUntouched("key-untouched", identifiable, "newValue");
        assertTrue(valueWasStored);
        assertEquals("newValue", service.get("key-untouched"));

        boolean valueWasStored2 = service.putIfUntouched("key-untouched", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", service.get("key-untouched"));
    }

//    @Test // TODO
//    @InSequence(120)
//    @OperateOnDeployment("dep2")
    public void testPutIfUntouchedOnDepB() {
        sleep(3000);

        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key-untouched");

        boolean valueWasStored2 = service.putIfUntouched("key-untouched", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", service.get("key-untouched"));
    }

    @Test
    @InSequence(130)
    @OperateOnDeployment("dep1")
    public void testGetAllOnDepA() {
        service.put("key11", "value1");
        service.put("key12", "value2");
        service.put("key13", "value3");

        Map<String, Object> map = service.getAll(Arrays.asList("key11", "key12"));
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key11"));
        assertEquals("value2", map.get("key12"));
    }

    @Test
    @InSequence(140)
    @OperateOnDeployment("dep2")
    public void testGetAllOnDepB() {
        sleep(3000);
        List<String> keys = Arrays.asList("key11", "key12");
        try {
            Map<String, Object> map = service.getAll(keys);
            assertEquals(2, map.size());
            assertEquals("value1", map.get("key11"));
            assertEquals("value2", map.get("key12"));
        } finally {
            service.deleteAll(keys);
        }
    }

    @Test
    @InSequence(150)
    @OperateOnDeployment("dep1")
    public void testDeleteOnDepA() {
        service.put("key21", "value");
        service.put("key22", "value");
        assertTrue(service.delete("key21"));
        assertFalse(service.contains("key21"));
    }

    @Test
    @InSequence(160)
    @OperateOnDeployment("dep2")
    public void testDeleteOnDepB() {
        sleep(3000);
        assertFalse(service.contains("key21"));
        assertTrue(service.contains("key22"));
        assertTrue(service.delete("key22"));
        assertFalse(service.contains("key22"));
    }

    @Test
    @InSequence(170)
    @OperateOnDeployment("dep1")
    public void testDeleteAllOnDepA() {
        service.put("key31", "value1");
        service.put("key32", "value2");
        service.put("key33", "value3");
        service.put("key34", "value4");
        service.put("key35", "value5");
        service.deleteAll(Arrays.asList("key31", "key32"));
        assertFalse(service.contains("key31"));
        assertFalse(service.contains("key32"));
        assertTrue(service.contains("key33"));
        assertTrue(service.contains("key34"));
    }

    @Test
    @InSequence(180)
    @OperateOnDeployment("dep2")
    public void testDeleteAllOnDepB() {
        sleep(3000);
        try {
            assertTrue(service.contains("key33"));
            assertTrue(service.contains("key34"));
            assertTrue(service.contains("key35"));

            service.deleteAll(Arrays.asList("key33", "key34"));

            assertFalse(service.contains("key33"));
            assertFalse(service.contains("key34"));
            assertTrue(service.contains("key35"));
        } finally {
            assertTrue(service.delete("key35"));
        }
    }

    @Test
    @InSequence(190)
    @OperateOnDeployment("dep1")
    public void testPutExpirationOnDepA() {
        service.put("key44", "value", Expiration.byDeltaMillis(3000));
        assertTrue(service.contains("key44"));
    }

    @Test
    @InSequence(200)
    @OperateOnDeployment("dep2")
    public void testPutExpirationOnDepB() {
        sleep(3000);
        assertTrue(service.contains("key44"));
        sleep(4000);
        assertFalse(service.contains("key44"));
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
        sleep(3000);
        try {
            long x = service.increment("increment-key", 4);
            assertEquals(25L, x);
        } finally {
            assertTrue(service.delete("increment-key"));
        }
    }

    private void sleep(long millis) {
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
        sleep(5000L);
        service.clearAll();
    }

}
