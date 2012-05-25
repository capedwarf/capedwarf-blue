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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.datastore.query.GAEKeyTransformer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class GAEKeyTransformerTestCase {

    private GAEKeyTransformer transformer = new GAEKeyTransformer();

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml");
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
}
