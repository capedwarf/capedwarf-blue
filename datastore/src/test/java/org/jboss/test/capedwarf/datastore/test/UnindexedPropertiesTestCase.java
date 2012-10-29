/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class UnindexedPropertiesTestCase extends SimpleTest {

    private static final Field PROPERTY_MAP_FIELD;
    static {
        try {
            PROPERTY_MAP_FIELD = Entity.class.getDeclaredField("propertyMap");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        PROPERTY_MAP_FIELD.setAccessible(true);
    }


    @Test
    public void testUnindexedProperties() throws Exception {
        Entity entity = new Entity("test");
        entity.setUnindexedProperty("unindexedString", "unindexedString");
        entity.setUnindexedProperty("unindexedList", new ArrayList<String>(Arrays.asList("listElement1", "listElement2", "listElement3")));
        entity.setUnindexedProperty("unindexedText", new Text("unindexedText"));
        entity.setUnindexedProperty("unindexedBlob", new Blob("unindexedBlob".getBytes()));
        entity.setProperty("text", new Text("text"));
        entity.setProperty("blob", new Blob("blob".getBytes()));

        Key key = service.put(entity);
        Entity entity2 = service.get(key);

        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedString")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedList")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedText")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedBlob")));
        assertTrue(isUnindexed(getRawProperty(entity2, "text")));
        assertTrue(isUnindexed(getRawProperty(entity2, "blob")));

        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("unindexedString", Query.FilterOperator.EQUAL, "unindexedString"))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("unindexedList", Query.FilterOperator.EQUAL, "listElement1"))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("unindexedText", Query.FilterOperator.EQUAL, "unindexedText"))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("unindexedText", Query.FilterOperator.EQUAL, new Text("unindexedText")))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("unindexedBlob", Query.FilterOperator.EQUAL, new Blob("unindexedBlob".getBytes())))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("text", Query.FilterOperator.EQUAL, "text"))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("text", Query.FilterOperator.EQUAL, new Text("text")))));
        assertNull(getResult(new Query("test").setFilter(new Query.FilterPredicate("blob", Query.FilterOperator.EQUAL, new Blob("blob".getBytes())))));
    }

    private Entity getResult(Query query) {
        return service.prepare(query).asSingleEntity();
    }

    private boolean isUnindexed(Object rawProperty) {
        return rawProperty.getClass().getName().endsWith("$UnindexedValue");
    }

    private Object getRawProperty(Entity entity, String propertyName) throws IllegalAccessException {
        return getRawPropertyMap(entity).get(propertyName);
    }

    private Map getRawPropertyMap(Entity entity) throws IllegalAccessException {
        return ((Map)PROPERTY_MAP_FIELD.get(entity));
    }
}
