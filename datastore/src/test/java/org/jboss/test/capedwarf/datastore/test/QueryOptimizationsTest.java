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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.RawValue;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Datastore querying optimizations tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@org.junit.experimental.categories.Category(All.class)
public class QueryOptimizationsTest extends QueryTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testKeysOnly() throws Exception {
        Entity john = createEntity("Person", 1)
            .withProperty("name", "John")
            .withProperty("surname", "Doe")
            .store();

        Query query = new Query("Person").setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);

        Entity entity = preparedQuery.asSingleEntity();
        assertEquals(john.getKey(), entity.getKey());
        assertNull(entity.getProperty("name"));
        assertNull(entity.getProperty("surname"));
    }

    @Test
    public void testProjections() throws Exception {
        Entity e = createEntity("Product", 1)
                .withProperty("price", 123L)
                .withProperty("percent", 0.123)
                .withProperty("x", -0.321)
                .withProperty("diff", -5L)
                .withProperty("weight", 10L)
                .store();

        Query query = new Query("Product")
                .addProjection(new PropertyProjection("price", Long.class))
                .addProjection(new PropertyProjection("percent", Double.class))
                .addProjection(new PropertyProjection("x", Double.class))
                .addProjection(new PropertyProjection("diff", Long.class));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());
        assertEquals(e.getProperty("price"), result.getProperty("price"));
        assertEquals(e.getProperty("percent"), result.getProperty("percent"));
        assertEquals(e.getProperty("x"), result.getProperty("x"));
        assertEquals(e.getProperty("diff"), result.getProperty("diff"));
        assertNull(result.getProperty("weight"));
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingProjectedProperty() throws Exception {
        Entity e1 = createEntity("Kind", 1)
            .withProperty("foo", "foo")
            .store();

        Entity e2 = createEntity("Kind", 2)
            .withProperty("bar", "bar")
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("foo", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingAllProjectedProperties() throws Exception {
        Entity e1 = createEntity("Kind", 1)
            .withProperty("foo", "foo")
            .withProperty("bar", "bar")
            .store();

        Entity e2 = createEntity("Kind", 2)
            .withProperty("foo", "foo")
            .store();

        Entity e3 = createEntity("Kind", 3)
            .withProperty("bar", "bar")
            .store();

        Entity e4 = createEntity("Kind", 4)
            .withProperty("baz", "baz")
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("foo", String.class))
            .addProjection(new PropertyProjection("bar", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionQueryReturnsEntitiesContainingProjectedPropertyEvenIfPropertyValueIsSetToNull() throws Exception {
        Entity e1 = createEntity("Kind", 1)
            .withProperty("foo", null)
            .store();

        Query query = new Query("Kind")
            .addProjection(new PropertyProjection("foo", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void testLongRawValue() throws Exception {
        RawValue raw = getRawValue(1000L);
        assertEquals(Long.valueOf(1000L), raw.getValue());
        assertEquals(Long.valueOf(1000L), raw.asStrictType(Long.class));
        assertEquals(Long.valueOf(1000L), raw.asType(Long.class));
        assertEquals(Long.valueOf(1000L), raw.asType(Integer.class));
        assertEquals(Long.valueOf(1000L), raw.asType(Short.class));
        assertEquals(Long.valueOf(1000L), raw.asType(Byte.class));
        assertEquals(new Date(1), raw.asType(Date.class));
        assertEquals(new Date(1), raw.asStrictType(Date.class));
        assertIAEThrownByAsStrictType(raw, Byte.class, Short.class, Integer.class);
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Byte.class, Short.class, Integer.class, Long.class, Date.class);
    }

    @Test
    public void testRatingRawValue() throws Exception {
        RawValue raw = getRawValue(new Rating(10));
        assertEquals(10L, raw.getValue());
        assertEquals(new Rating(10), raw.asStrictType(Rating.class));
        assertEquals(new Rating(10), raw.asType(Rating.class));
        assertEquals(Long.valueOf(10L), raw.asStrictType(Long.class));
        assertEquals(10L, raw.asType(Long.class));
        assertEquals(10L, raw.asType(Integer.class));
        assertEquals(10L, raw.asType(Short.class));
        assertEquals(10L, raw.asType(Byte.class));
        assertEquals(new Date(0), raw.asType(Date.class));
//        assertEquals(new Date(0), raw.asStrictType(Date.class));  TODO: check against appspot
        assertIAEThrownByAsStrictType(raw, Byte.class, Short.class, Integer.class);
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Byte.class, Short.class, Integer.class, Long.class, Date.class, Rating.class);
    }

    @Test
    public void testDateRawValue() throws Exception {
        Date now = new Date();
        long microSeconds = now.getTime() * 1000;
        RawValue raw = getRawValue(now);
        assertEquals(microSeconds, raw.getValue());
        assertEquals(now, raw.asStrictType(Date.class));
        assertEquals(now, raw.asType(Date.class));
        assertEquals(Long.valueOf(microSeconds), raw.asStrictType(Long.class));
        assertEquals(microSeconds, raw.asType(Long.class));
        assertEquals(microSeconds, raw.asType(Integer.class));
        assertEquals(microSeconds, raw.asType(Short.class));
        assertEquals(microSeconds, raw.asType(Byte.class));
        assertIAEThrownByAsStrictType(raw, Byte.class, Short.class, Integer.class);
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Byte.class, Short.class, Integer.class, Long.class, Date.class);
    }

    @Test
    public void testDoubleRawValue() throws Exception {
        RawValue raw = getRawValue(2.0);
        assertEquals(2.0, raw.getValue());
        assertEquals(Double.valueOf(2.0), raw.asStrictType(Double.class));
        assertEquals(2.0, raw.asType(Double.class));
        assertIAEThrownByAsStrictType(raw, Float.class);
        assertEquals(2.0, raw.asType(Float.class));
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Double.class, Float.class);
    }

    @Test
    public void testStringRawValue() throws Exception {
        String string = "sip string";
        RawValue raw = getRawValue(string);
        assertEquals(byte[].class, raw.getValue().getClass());
        assertArrayEquals(string.getBytes(), (byte[]) raw.getValue());
        assertEquals(string, raw.asType(String.class));
        assertEquals(string, raw.asStrictType(String.class));
        assertEquals(new Text(string), raw.asType(Text.class));
        assertEquals(new Text(string), raw.asStrictType(Text.class));
        assertEquals(new Link(string), raw.asType(Link.class));
        assertEquals(new Link(string), raw.asStrictType(Link.class));
        assertEquals(new ShortBlob(string.getBytes()), raw.asType(ShortBlob.class));
//        assertEquals(new Blob(string.getBytes()), raw.asType(Blob.class));  // TODO: check against appspot
        assertEquals(new Category(string), raw.asType(Category.class));
        assertEquals(new PhoneNumber(string), raw.asType(PhoneNumber.class));
        assertEquals(new PostalAddress(string), raw.asType(PostalAddress.class));
        assertEquals(new Email(string), raw.asType(Email.class));
        assertEquals(new IMHandle(IMHandle.Scheme.sip, "string"), raw.asType(IMHandle.class));
        assertEquals(new Link(string), raw.asType(Link.class));
        assertEquals(new BlobKey(string), raw.asType(BlobKey.class));
        assertIAEThrownByBothAsTypeMethods(raw, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Boolean.class, Date.class,
            GeoPt.class, User.class, Rating.class, Key.class);
    }

    @Test
    public void testNumericStringRawValue() throws Exception {
        RawValue raw = getRawValue("123");
        assertEquals(byte[].class, raw.getValue().getClass());
        assertArrayEquals("123".getBytes(), (byte[]) raw.getValue());
        assertIAEThrownByBothAsTypeMethods(raw, Integer.class, Long.class, Float.class, Double.class, Date.class);
    }

    @Test
    public void testPhoneNumberRawValue() throws Exception {
        String string = "123";
        RawValue raw = getRawValue(new PhoneNumber(string));
        assertEquals(byte[].class, raw.getValue().getClass());
        assertArrayEquals(string.getBytes(), (byte[]) raw.getValue());
        assertEquals(new PhoneNumber(string), raw.asStrictType(PhoneNumber.class));
        assertEquals(new PhoneNumber(string), raw.asType(PhoneNumber.class));
        assertEquals(new Text(string), raw.asType(Text.class));
        assertEquals(new Text(string), raw.asStrictType(Text.class));
        assertEquals(new Link(string), raw.asType(Link.class));
        assertEquals(new Link(string), raw.asStrictType(Link.class));
        assertEquals(new ShortBlob(string.getBytes()), raw.asType(ShortBlob.class));
//        assertEquals(new Blob(string.getBytes()), raw.asType(Blob.class)); // TODO: check against appspot
        assertEquals(new Category(string), raw.asType(Category.class));
        assertEquals(new PhoneNumber(string), raw.asType(PhoneNumber.class));
        assertEquals(new PostalAddress(string), raw.asType(PostalAddress.class));
        assertEquals(new Email(string), raw.asType(Email.class));
        assertEquals(new Link(string), raw.asType(Link.class));
        assertEquals(new BlobKey(string), raw.asType(BlobKey.class));
        assertIAEThrownByBothAsTypeMethods(raw, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Boolean.class, Date.class,
            GeoPt.class, User.class, Rating.class, Key.class);
    }

    @Test
    public void testBooleanRawValue() throws Exception {
        RawValue raw = getRawValue(true);
        assertEquals(true, raw.getValue());
        assertEquals(true, raw.asStrictType(Boolean.class));
        assertEquals(true, raw.asType(Boolean.class));
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Boolean.class);
    }

    @Test
    public void testUserRawValue() throws Exception {
        User user = new User("someone@gmail.com", "gmail.com");
        RawValue raw = getRawValue(user);
        assertEquals(user, raw.getValue());
        assertEquals(user, raw.asStrictType(User.class));
        assertEquals(user, raw.asType(User.class));
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, User.class);
    }

    @Test
    public void testKeyRawValue() throws Exception {
        Key key = KeyFactory.createKey("kind", 1);
        RawValue raw = getRawValue(key);
        assertEquals(key, raw.getValue());
        assertEquals(key, raw.asStrictType(Key.class));
        assertEquals(key, raw.asType(Key.class));
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, Key.class);
    }

    @Test
    public void testGeoPtRawValue() throws Exception {
        GeoPt geoPt = new GeoPt(1f, 2f);
        RawValue raw = getRawValue(geoPt);
        assertEquals(geoPt, raw.getValue());
        assertEquals(geoPt, raw.asStrictType(GeoPt.class));
        assertEquals(geoPt, raw.asType(GeoPt.class));
        assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(raw, GeoPt.class);
    }

    @Ignore("verify test against appspot")
    @Test
    public void testBlobKeyRawValue() throws Exception {
        BlobKey blobKey = new BlobKey("123");
        RawValue raw = getRawValue(blobKey);
        assertEquals(byte[].class, raw.getValue().getClass());
        assertArrayEquals("123".getBytes(), (byte[]) raw.getValue());
        assertEquals(blobKey, raw.asStrictType(BlobKey.class));
        assertEquals(blobKey, raw.asType(BlobKey.class));
        assertIAEThrownByBothAsTypeMethods(raw, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, Boolean.class, Date.class,
            GeoPt.class, User.class, Rating.class, Key.class);
    }

    @Test
    public void testRawValueReturnsByteArrayValueForAllStringTypes() throws Exception {
        // NOTE: Text and Blob types are not indexed
        assertEquals(byte[].class, getRawValue("string").getValue().getClass());
        assertEquals(byte[].class, getRawValue(new ShortBlob("string".getBytes())).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new PostalAddress("string")).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new PhoneNumber("string")).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new Email("string")).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new IMHandle(IMHandle.Scheme.sip, "string")).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new Link("string")).getValue().getClass());
        assertEquals(byte[].class, getRawValue(new Category("string")).getValue().getClass());
    }

    @Test
    public void testByteArrayReturnedByStringRawValueHasUTF8Encoding() throws Exception {
        assertArrayEquals("čćž".getBytes("UTF-8"), (byte[]) getRawValue("čćž").getValue());
    }

    private RawValue getRawValue(Object value) {
        Entity e = createEntity("RawValueTest", 1)
            .withProperty("prop", value)
            .store();

        Query query = new Query("RawValueTest")
            .addProjection(new PropertyProjection("prop", null));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());

        return (RawValue) result.getProperty("prop");
    }

    private void assertIAEThrownByBothAsTypeMethodsForAllTypesExceptFor(RawValue raw, Class<?>... types) {
        HashSet<Class<?>> allTypes = new HashSet<Class<?>>(Arrays.asList((Class<?>)
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class,
            Boolean.class, Date.class,
            String.class, Text.class, ShortBlob.class, Blob.class, GeoPt.class, PostalAddress.class, PhoneNumber.class,
            Email.class, User.class, IMHandle.class, Link.class, Category.class, Rating.class, Key.class, BlobKey.class));

        allTypes.removeAll(Arrays.asList(types));

        assertIAEThrownByBothAsTypeMethods(raw, allTypes.toArray(new Class[allTypes.size()]));
    }

    private void assertIAEThrownByBothAsTypeMethods(RawValue rawValue, Class<?>... types) {
        assertIAEThrownByAsStrictType(rawValue, types);
        assertIAEThrownByAsType(rawValue, types);
    }

    private void assertIAEThrownByAsType(RawValue rawValue, Class<?>... types) {
        for (Class<?> type : types) {
            assertIAEThrownByAsType(rawValue, type);
        }
    }

    private void assertIAEThrownByAsType(RawValue rawValue, Class<?> type) {
        try {
            rawValue.asType(type);
            fail("Expected RawValue.asType(" + type.getSimpleName() + ") to throw IllegalArgumentException");
        } catch (IllegalArgumentException ok) {
        }
    }

    private void assertIAEThrownByAsStrictType(RawValue rawValue, Class<?>... types) {
        for (Class<?> type : types) {
            assertIAEThrownByAsStrictType(rawValue, type);
        }
    }

    private void assertIAEThrownByAsStrictType(RawValue rawValue, Class<?> type) {
        try {
            rawValue.asStrictType(type);
            fail("Expected RawValue.asStrictType(" + type.getSimpleName() + ") to throw IllegalArgumentException");
        } catch (IllegalArgumentException ok) {
        }
    }

    @Test
    public void testProjectionTypeMismatch() throws Exception {
        Entity e = createEntity("foo", 1)
            .withProperty("stringProperty", "foo")
            .store();

        Query query = new Query("foo")
            .addProjection(new PropertyProjection("stringProperty", Integer.class));

        PreparedQuery preparedQuery = service.prepare(query);

        thrown.expect(IllegalArgumentException.class);
        preparedQuery.asSingleEntity();
    }

    @Test
    public void testProjectionOfCollectionProperties() throws Exception {
        Entity e = createEntity("test", 1)
            .withProperty("prop", Arrays.asList("bbb", "ccc", "aaa"))
            .withProperty("prop2", Arrays.asList("xxx", "yyy"))
            .store();

        Query query = new Query("test")
            .addProjection(new PropertyProjection("prop", String.class))
            .addSort("prop");

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(withDefaults());
        assertEquals(3, results.size());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);
        Entity thirdResult = results.get(2);

        assertEquals(e.getKey(), firstResult.getKey());
        assertEquals(e.getKey(), secondResult.getKey());
        assertEquals(e.getKey(), thirdResult.getKey());
        assertEquals("aaa", firstResult.getProperty("prop"));
        assertEquals("bbb", secondResult.getProperty("prop"));
        assertEquals("ccc", thirdResult.getProperty("prop"));
    }

    @Test
    public void testProjectionOfCollectionPropertyWithFilterOnCollectionProperty() throws Exception {
        Entity e = createEntity("Product", 1)
            .withProperty("name", Arrays.asList("aaa", "bbb"))
            .withProperty("price", Arrays.asList(10L, 20L))
            .store();

        Query query = new Query("Product")
            .addProjection(new PropertyProjection("name", String.class))
            .setFilter(new Query.FilterPredicate("price", GREATER_THAN, 0L))
            .addSort("price")
            .addSort("name");

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(withDefaults());
        assertEquals(4, results.size());

        assertEquals(e.getKey(), results.get(0).getKey());
        assertEquals(e.getKey(), results.get(1).getKey());
        assertEquals(e.getKey(), results.get(2).getKey());
        assertEquals(e.getKey(), results.get(3).getKey());

        assertEquals("aaa", results.get(0).getProperty("name"));
        assertEquals("bbb", results.get(1).getProperty("name"));
        assertEquals("aaa", results.get(2).getProperty("name"));
        assertEquals("bbb", results.get(3).getProperty("name"));
    }

    @Test
    public void testProjectionQueriesHandleEntityModificationProperly() throws Exception {
        Entity e = createEntity("test", 1)
            .withProperty("prop", Arrays.asList("aaa", "bbb", "ccc"))
            .store();

        Query query = new Query("test")
            .addProjection(new PropertyProjection("prop", String.class))
            .addSort("prop");

        assertEquals(3, service.prepare(query).asList(withDefaults()).size());

        e = createEntity(e.getKey())
            .withProperty("prop", Arrays.asList("aaa", "bbb"))
            .store();

        assertEquals(2, service.prepare(query).asList(withDefaults()).size());

        service.delete(e.getKey());

        assertEquals(0, service.prepare(query).asList(withDefaults()).size());
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingProjections() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingKeysOnly() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .store();

        Query query = new Query("Product")
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testEntityOnlyContainsProjectedProperties() throws Exception {
        Entity a = createEntity("Product", 1)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity b = createEntity("Product", 2)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        Entity firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(1, firstResult.getProperties().size());
        assertEquals("price", firstResult.getProperties().keySet().iterator().next());

        query = new Query("Product")
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(0, firstResult.getProperties().size());
    }

    private void assertResultsInOrder(Query query, Entity a, Entity b) {
        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);

        assertEquals(b.getKey(), firstResult.getKey());
        assertEquals(a.getKey(), secondResult.getKey());
    }
}
