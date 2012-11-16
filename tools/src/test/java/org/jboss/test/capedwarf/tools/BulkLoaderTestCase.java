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

package org.jboss.test.capedwarf.tools;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.capedwarf.tools.BulkLoader;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author Marko Luksa
 * @author Ales Justin
 */
@RunWith(Arquillian.class)
public class BulkLoaderTestCase extends BaseTest {

    private static File dumpFile;

    @ArquillianResource
    private transient Deployer deployer;

    @Deployment (name = "deployment", managed = false)
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setWebXmlFile("web.xml");
        return getCapedwarfDeployment(context);
    }

    @BeforeClass
    public static void beforeClass() {
//        dumpFile = new File("C:/temp/testdump.sql3");
        dumpFile = new File(System.getProperty("java.io.tmpdir"), "dump.sql3");
        assertFalse("Please delete dump file manually: " + dumpFile.getAbsolutePath(), dumpFile.exists());
    }

    @Test
    @InSequence(1)
    @RunAsClient
    public void deployApplication() throws Exception {
        deployer.deploy("deployment");
    }

    @Test
    @InSequence(10)
    public void generateEntities() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (int i = 0; i < 10; i++) {
            String namespace = "namespace" + i;
            NamespaceManager.set(namespace);
            for (int j = 0; j < 10; j++) {
                String kind = "kind" + i;
                for (int id = 1; id <= 10; id++) {
                    Entity entity = new Entity(kind, id);
                    entity.setProperty("foo", id);
                    datastore.put(entity);
                }
            }
        }
    }

    @Test
    @InSequence(20)
    @RunAsClient
    public void download(@ArquillianResource URL url) throws IOException {
        BulkLoader.main(new String[]{"dump", "--url=" + url + "/remote_api", "--filename=" + dumpFile.getAbsolutePath()});
        assertTrue("dump file does not exist", dumpFile.exists());
        assertTrue("dump file is empty", dumpFile.length() > 0);
    }

    @Test
    @InSequence(30)
    public void deleteAllEntities() {
        cleanup();
    }

    @Test
    @InSequence(40)
    public void testEntityDoesntExist() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // take middle key
        NamespaceManager.set("namespace5");
        Key key = KeyFactory.createKey("kind5", 5);
        Assert.assertTrue(datastore.get(Collections.singleton(key)).isEmpty());
    }

    @Test
    @InSequence(50)
    @RunAsClient
    public void upload(@ArquillianResource URL url) throws IOException {
        BulkLoader.main(new String[]{"upload", "--url=" + url + "/remote_api", "--filename=" + dumpFile.getAbsolutePath()});
    }

    @Test
    @InSequence(60)
    public void checkAllEntitiesExist() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (int i = 0; i < 10; i++) {
            String namespace = "namespace" + i;
            NamespaceManager.set(namespace);
            for (int j = 0; j < 10; j++) {
                String kind = "kind" + i;
                for (int id = 1; id <= 10; id++) {
                    Key key = KeyFactory.createKey(kind, id);
                    try {
                        datastore.get(key);
                    } catch (EntityNotFoundException e) {
                        fail("Could not find entity " + key);
                    }
                }
            }
        }
    }

    @Test
    @InSequence(70)
    public void cleanupEntities() {
        cleanup();
    }

    @AfterClass
    public static void afterClass() {
        Assert.assertTrue(dumpFile.delete());
    }

    protected void cleanup() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (int i = 0; i < 10; i++) {
            String namespace = "namespace" + i;
            NamespaceManager.set(namespace);
            for (int j = 0; j < 10; j++) {
                String kind = "kind" + i;
                for (int id = 1; id <= 10; id++) {
                    datastore.delete(KeyFactory.createKey(kind, id));
                }
            }
        }
    }

}
