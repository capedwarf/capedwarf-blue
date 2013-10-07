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

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.datastore.query.GAEKeyTransformer;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class GAEKeyTransformerTest {

    private GAEKeyTransformer transformer = new GAEKeyTransformer();

    @Before
    public void setUp() throws Exception {
        ApiProxy.setEnvironmentForCurrentThread(new MockEnvironment());
    }

    @Test
    public void testKindAndId() throws Exception {
        Key key = KeyFactory.createKey("Test", 1);
        assertEquals(key, transformer.fromString(transformer.toString(key)));

        Key key2 = KeyFactory.createKey("Test", 2);
        assertFalse(key2.equals(transformer.fromString(transformer.toString(key))));
    }

    @Test
    public void testKindAndName() throws Exception {
        Key key = KeyFactory.createKey("Test", "name");
        assertEquals(key, transformer.fromString(transformer.toString(key)));
    }

    @Test
    public void testParentKindAndId() throws Exception {
        Key parent = KeyFactory.createKey("Parent", 1);
        Key key = KeyFactory.createKey(parent, "Test", 2);
        assertEquals(key, transformer.fromString(transformer.toString(key)));
    }

    @Test
    public void testParentKindAndName() throws Exception {
        Key parent = KeyFactory.createKey("Parent", 1);
        Key key = KeyFactory.createKey(parent, "Test", "name");
        assertEquals(key, transformer.fromString(transformer.toString(key)));
    }

    @Test
    public void testKeyTransformerCorrectlyHandlesNamespaces() throws Exception {
        GAEKeyTransformer transformer = new GAEKeyTransformer();

        NamespaceManager.set("one");
        Key key1 = KeyFactory.createKey("Test", 1);
        String string = transformer.toString(key1);

        NamespaceManager.set("two");
        Object transformedKey1 = transformer.fromString(string);

        assertEquals(key1, transformedKey1);

        Key key2 = KeyFactory.createKey("Test", 1);
        Object transformedKey2 = transformer.fromString(transformer.toString(key2));

        assertEquals(key2, transformedKey2);
        assertFalse(key1.equals(key2));
    }

    private static class MockEnvironment implements ApiProxy.Environment {

        private HashMap<String,Object> attributes = new HashMap<String, Object>();

        public String getAppId() {
            return "test";
        }

        public String getVersionId() {
            return "1";
        }

        @Override
        public String getModuleId() {
            return "default";
        }

        public String getEmail() {
            return null;
        }

        public boolean isLoggedIn() {
            return false;
        }

        public boolean isAdmin() {
            return false;
        }

        public String getAuthDomain() {
            return null;
        }

        public String getRequestNamespace() {
            return null;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public long getRemainingMillis() {
            return 0;
        }
    }
}
