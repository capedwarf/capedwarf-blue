/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.capedwarf.datastore.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreConfig;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.datastore.JBossAsyncDatastoreService;
import org.jboss.capedwarf.datastore.JBossDatastoreService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class SmokeTestCase extends AbstractTest {

    @Test
    public void testFactoryPassesConfigToDatastoreServiceInstance() throws Exception {
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
        JBossDatastoreService service = (JBossDatastoreService) DatastoreServiceFactory.getDatastoreService(config);
        assertEquals(config, service.getConfig());

        JBossAsyncDatastoreService asyncService = (JBossAsyncDatastoreService) DatastoreServiceFactory.getAsyncDatastoreService(config);
        assertEquals(config, asyncService.getConfig());
    }

    @Test
    public void testFactorySupportsDeprecatedMethods() throws Exception {
        DatastoreConfig oldConfig = DatastoreServiceFactory.getDefaultDatastoreConfig();
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService(oldConfig);
        assertTrue(datastoreService instanceof JBossDatastoreService);
    }

    @Test
    public void putStoresEntity() throws Exception {
        Entity entity = createTestEntity();
        service.put(entity);
        assertStoreContains(entity);
    }

    @Test
    public void putStoresAllGivenEntities() throws Exception {
        Collection<Entity> entities = createTestEntities();
        for (Entity e : entities) {
            service.put(e);
        }
        assertStoreContainsAll(entities);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getThrowsNotFoundExceptionWhenKeyIsNotFound() throws Exception {
        Key nonExistingKey = KeyFactory.createKey("NonExistingKey", 1);
        service.get(nonExistingKey);
    }

    @Test
    public void batchGetReturnsOnlyExistingKeysInMap() throws Exception {
        Key existingKey = KeyFactory.createKey("batch", "existing");
        Key nonExistingKey = KeyFactory.createKey("batch", "nonExisting");
        service.put(new Entity(existingKey));

        Map<Key,Entity> map = service.get(Arrays.asList(existingKey, nonExistingKey));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(existingKey));
    }

    @Test
    public void deleteRemovesEntityFromStore() throws Exception {
        Entity entity = createTestEntity();
        Key key = entity.getKey();
        service.put(entity);

        service.delete(key);
        assertStoreDoesNotContain(key);
    }

    @Test
    public void deleteRemovesAllGivenEntities() throws Exception {
        Collection<Entity> entities = createTestEntities();
        Collection<Key> keys = extractKeys(entities);
        for (Entity e : entities) {
            service.put(e);
        }

        for (Key k : keys) {
            service.delete(k);
        }
        assertStoreDoesNotContain(keys);
    }

    @Test
    public void queriesDontReturnDeletedEntities() throws Exception {
        Entity entity = createTestEntity("KIND");
        Key key = entity.getKey();
        service.put(entity);

        service.delete(key);

        List<Entity> entities = service.prepare(new Query("KIND")).asList(FetchOptions.Builder.withDefaults());
        assertEquals(0, entities.size());
    }


}
