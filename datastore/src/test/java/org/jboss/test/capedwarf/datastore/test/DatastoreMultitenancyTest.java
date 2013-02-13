/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.test.capedwarf.datastore.test;

import java.util.Collections;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class DatastoreMultitenancyTest extends SimpleTestBase {

    private String originalNamespace;

    @Before
    public void setUp() {
        super.setUp();
        originalNamespace = NamespaceManager.get();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        NamespaceManager.set(originalNamespace);
    }

    @Test
    public void testKeysCreatedUnderDifferentNamespacesAreNotEqual() throws Exception {
        NamespaceManager.set("one");
        Key key1 = KeyFactory.createKey("Test", 1);

        NamespaceManager.set("two");
        Key key2 = KeyFactory.createKey("Test", 1);

        assertFalse(key1.equals(key2));
    }

    @Test
    public void testTwoEntitiesWithSameKeyButDifferentNamespaceDontOverwriteEachOther() throws EntityNotFoundException {
        NamespaceManager.set("one");
        Key key1 = KeyFactory.createKey("Test", 1);
        Entity entity1 = new Entity(key1);
        service.put(entity1);
        assertEquals(entity1, service.get(key1));

        NamespaceManager.set("two");
        Key key2 = KeyFactory.createKey("Test", 1);

        try {
            Entity entity = service.get(key2);
            fail("Expected no entity in namespace 'two'; but got: " + entity);
        } catch (EntityNotFoundException e) {
        }

        Entity entity2 = new Entity(key2);
        service.put(entity2);
        assertEquals(entity2, service.get(key2));

        NamespaceManager.set("one");
        assertEquals(entity1, service.get(key1));

        service.delete(key1);
        service.delete(key2);
    }

    @Test
    public void testQueriesOnlyReturnResultsInCurrentNamespace() {
        NamespaceManager.set("one");
        Entity fooOne = new Entity("foo");
        service.put(fooOne);

        NamespaceManager.set("two");
        Entity fooTwo = new Entity("foo");
        service.put(fooTwo);

        List<Entity> listTwo = service.prepare(new Query("foo")).asList(withDefaults());
        assertEquals(Collections.singletonList(fooTwo), listTwo);

        NamespaceManager.set("one");
        List<Entity> listOne = service.prepare(new Query("foo")).asList(withDefaults());
        assertEquals(Collections.singletonList(fooOne), listOne);

        service.delete(fooOne.getKey());
        service.delete(fooTwo.getKey());
    }

}
