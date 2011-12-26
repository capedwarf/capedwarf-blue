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

import com.google.appengine.api.datastore.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.capedwarf.datastore.JBossDatastoreService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class AbstractTest {

    protected DatastoreService service;

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(AbstractTest.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine/appengine-web.xml", "appengine-web.xml");
    }

    @Before
    public void setUp() {
        service = DatastoreServiceFactory.getDatastoreService();
    }

    @After
    public void tearDown() {
        ((JBossDatastoreService) service).clearCache();
    }

    protected Collection<Key> extractKeys(Collection<Entity> entities) {
        List<Key> keys = new ArrayList<Key>();
        for (Entity entity : entities) {
            keys.add(entity.getKey());
        }
        return keys;
    }

    protected Collection<Entity> createTestEntities() {
        return Arrays.asList(createTestEntity("One", 1), createTestEntity("Two", 2), createTestEntity("Three", 3));
    }

    protected void assertStoreContainsAll(Collection<Entity> entities) throws EntityNotFoundException {
        for (Entity entity : entities) {
            assertStoreContains(entity);
        }
    }

    protected void assertStoreContains(Entity entity) throws EntityNotFoundException {
        Entity lookup = service.get(entity.getKey());
        Assert.assertNotNull(lookup);
        Assert.assertEquals(entity, lookup);
    }

    protected void assertStoreDoesNotContain(Entity entity) throws EntityNotFoundException {
        assertStoreDoesNotContain(entity.getKey());
    }

    protected void assertStoreDoesNotContain(Collection<Key> keys) throws EntityNotFoundException {
        for (Key key : keys) {
            assertStoreDoesNotContain(key);
        }
    }

    protected void assertStoreDoesNotContain(Key key) throws EntityNotFoundException {
        try {
            Entity storedEntity = service.get(key);
            Assert.fail("expected the datastore not to contain anything under key " + key + ", but it contained the entity " + storedEntity);
        } catch (EntityNotFoundException e) {
            // pass
        }
    }

    protected Entity createTestEntity() {
        return createTestEntity("KIND", 1);
    }

    protected Entity createTestEntity(String kind, int id) {
        Key key = KeyFactory.createKey(kind, id);
        Entity entity = new Entity(key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

}
