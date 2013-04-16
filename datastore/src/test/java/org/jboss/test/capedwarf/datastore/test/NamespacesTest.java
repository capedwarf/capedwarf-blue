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
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.datastore.NamespaceServiceFactory;
import org.jboss.capedwarf.datastore.NamespaceServiceInternal;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class NamespacesTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment(TestContext.withMetadata());
    }

    @Test
    public void testInternalAPI() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        if (!isJBossImpl(ds))
            return;

        Set<Key> keys = new HashSet<Key>();
        try {
            ds.put(new Entity("NO_NS"));

            NamespaceManager.set("NS1");
            Entity entity = new Entity("NS1_KIND");
            Key k1 = ds.put(entity);

            NamespaceManager.set("NS2");
            ds.put(new Entity("NS2_KIND"));

            sync(1000);

            NamespaceServiceInternal ns = NamespaceServiceFactory.getNamespaceService();

            Set<String> namespaces = ns.getNamespaces();
            assertEquals(asSet("", "NS1", "NS2"), namespaces);

            Set<String> kinds = ns.getKindsPerNamespace("");
            assertEquals(asSet("NO_NS"), kinds);
            kinds = ns.getKindsPerNamespace("NS1");
            assertEquals(asSet("NS1_KIND"), kinds);
            kinds = ns.getKindsPerNamespace("NS2");
            assertEquals(asSet("NS2_KIND"), kinds);


            // TODO: this isn't implemented yet
//            keys.remove(k1);
//            ds.delete(k1);
//            sync(1000);
//
//            namespaces = ns.getNamespaces();
//            Assert.assertEquals(asSet("", "NS2"), namespaces);
//
//            kinds = ns.getKindsPerNamespace("NS1");
//            Assert.assertEquals(new HashSet<String>(), kinds);
        } finally {
            ds.delete(keys);
        }
    }

    private static <E> Set<E> asSet(E... values) {
        return new HashSet<E>(Arrays.asList(values));
    }
}
