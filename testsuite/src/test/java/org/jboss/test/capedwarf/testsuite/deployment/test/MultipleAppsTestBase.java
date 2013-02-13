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

package org.jboss.test.capedwarf.testsuite.deployment.test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import junit.framework.Assert;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class MultipleAppsTestBase extends TestBase {
    protected static WebArchive getDeployment(String suffix) {
        final TestContext context = new TestContext("test-" + suffix + ".war");
        context.setAppEngineWebXmlFile("appengine-web-" + suffix + ".xml");
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClass(MultipleAppsTestBase.class);
        return war;
    }

    protected void allTests(boolean empty) throws Exception {
        testEmptyDS(empty);
        testDSTouch();
    }

    protected void testEmptyDS(boolean empty) throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("MKind");
        PreparedQuery pq = ds.prepare(query);
        Assert.assertEquals(!empty, pq.asIterator().hasNext());
    }

    protected void testDSTouch() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key key = ds.put(new Entity("MKind"));
        Assert.assertTrue(ds.get(key) != null);
    }

    protected void cleanup() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        for (Entity e : ds.prepare(new Query().setKeysOnly()).asIterable()) {
            ds.delete(e.getKey());
        }
    }
}
