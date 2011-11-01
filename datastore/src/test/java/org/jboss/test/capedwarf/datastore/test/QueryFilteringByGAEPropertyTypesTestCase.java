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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests if all Google AppEngine types can be stored as properties of Entity and if queries filtered on those
 * properties behave correctly for all filter operators (EQUAL, NOT_EQUAL, GREATER_THAN, ...)
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryFilteringByGAEPropertyTypesTestCase extends QueryTestCase {

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testTextProperty() {
        testEqualityQueries(new Text("foo"), new Text("bar"));
        testInequalityQueries(new Text("aaa"), new Text("bbb"), new Text("ccc"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testPhoneNumberProperty() {
        testEqualityQueries(new PhoneNumber("foo"), new PhoneNumber("bar"));
        testInequalityQueries(new PhoneNumber("111"), new PhoneNumber("222"), new PhoneNumber("333"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testPostalAddressProperty() {
        testEqualityQueries(new PostalAddress("foo"), new PostalAddress("bar"));
        testInequalityQueries(new PostalAddress("aaa"), new PostalAddress("bbb"), new PostalAddress("ccc"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testEmailProperty() {
        testEqualityQueries(new PostalAddress("foo@foo.com"), new PostalAddress("bar@bar.com"));
        testInequalityQueries(new PostalAddress("aaa@foo.com"), new PostalAddress("bbb@foo.com"), new PostalAddress("ccc@foo.com"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testUserProperty() {
        testEqualityQueries(new User("foo@foo.com", "authDomain", "userId", "federatedIdentity"), new User("bar@bar.com", "authDomain", "userId", "federatedIdentity"));
        testInequalityQueries(new User("aaa@foo.com", "authDomain"), new User("bbb@foo.com", "authDomain"), new User("ccc@foo.com", "authDomain"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testLinkProperty() {
        testEqualityQueries(new Link("http://foo.com"), new Link("http://bar.com"));
        testInequalityQueries(new Link("http://aaa.com"), new Link("http://bbb.com"), new Link("http://ccc.com"));
    }

    @Test
    public void testKeyProperty() {
        testEqualityQueries(KeyFactory.createKey("foo", "foo"), KeyFactory.createKey("bar", "bar"));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testRatingProperty() {
        testEqualityQueries(new Rating(1), new Rating(2));
        testInequalityQueries(new Rating(1), new Rating(2), new Rating(3));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testGeoPtProperty() {
        testEqualityQueries(new GeoPt(45f, 15f), new GeoPt(50f, 20f));
        testInequalityQueries(new GeoPt(20f, 10f), new GeoPt(30f, 10f), new GeoPt(40f, 10f));
        testInequalityQueries(new GeoPt(0f, 10f), new GeoPt(0f, 20f), new GeoPt(0f, 30f));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testCategoryProperty() {
        testEqualityQueries(new Category("foo"), new Category("bar"));
        testInequalityQueries(new Category("aaa"), new Category("bbb"), new Category("ccc"));
    }

    @Test
    public void testIMHandleProperty() {
        testEqualityQueries(new IMHandle(IMHandle.Scheme.xmpp, "foo@foo.com"), new IMHandle(IMHandle.Scheme.xmpp, "bar@bar.com"));
    }

    @Test
    public void testShortBlobProperty() {
        testEqualityQueries(new ShortBlob("foo".getBytes()), new ShortBlob("bar".getBytes()));
    }

    @Test
    public void testBlobProperty() {
        testEqualityQueries(new Blob("foo".getBytes()), new Blob("bar".getBytes()));
    }

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void testBlobKeyProperty() {
        testEqualityQueries(new BlobKey("foo"), new BlobKey("bar"));
        testInequalityQueries(new BlobKey("aaa"), new BlobKey("bbb"), new BlobKey("ccc"));
    }

}
