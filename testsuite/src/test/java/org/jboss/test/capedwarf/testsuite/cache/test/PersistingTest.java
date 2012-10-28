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

package org.jboss.test.capedwarf.testsuite.cache.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import junit.framework.Assert;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.testsuite.AbstractTest;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class PersistingTest extends AbstractTest {
    protected static WebArchive getBaseDeployment() {
        return getCapedwarfDeployment().addClasses(PersistingTest.class, AbstractTest.class);
    }

    @Test
    public void testPersisting() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        // ignore the test for non JBoss impl
        if (ds.getClass().getName().contains(".jboss.") == false)
            return;

        final Long id = readMarker();
        if (id != null) {
            Entity entity = ds.get(KeyFactory.createKey("Persisting", id));
            try {
                Assert.assertNotNull(entity);
                Assert.assertEquals("bar", entity.getProperty("foo"));
            } finally {
                if (entity != null) {
                    ds.delete(entity.getKey()); // proper remove
                }
            }
        } else {
            Entity entity = new Entity("Persisting");
            entity.setProperty("foo", "bar");
            ds.put(entity);
            writeMarker(entity.getKey().getId());
            ignoreTearDown = true;
        }
    }

    protected File getMarker() {
        return new File(System.getProperty("jboss.server.data.dir"), "marker.cd");
    }

    protected Long readMarker() throws Exception {
        final File marker = getMarker();
        if (marker.exists() == false)
            return null;

        long id;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(marker));
            try {
                id = Long.parseLong(reader.readLine());
            } finally {
                reader.close();
            }
        } finally {
            Assert.assertTrue(marker.delete());
        }
        return id;
    }

    protected void writeMarker(long id) throws Exception {
        FileOutputStream fos = new FileOutputStream(getMarker());
        try {
            fos.write(Long.toString(id).getBytes());
            fos.flush();
        } finally {
            fos.close();
        }
    }
}
