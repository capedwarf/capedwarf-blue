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

import com.google.appengine.api.datastore.DatastoreConfig;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.datastore.JBossAsyncDatastoreService;
import org.jboss.capedwarf.datastore.JBossDatastoreService;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class ConfigTestCase extends QueryTestCase {

    @Test
    public void testFactoryPassesConfigToDatastoreServiceInstance() throws Exception {
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
        JBossDatastoreService service = (JBossDatastoreService) DatastoreServiceFactory.getDatastoreService(config);
        assertEquals(config, service.getConfig());
    }

    @Test
    public void testFactoryPassesConfigToAsyncDatastoreServiceInstance() throws Exception {
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
        JBossAsyncDatastoreService service = (JBossAsyncDatastoreService) DatastoreServiceFactory.getAsyncDatastoreService(config);
        assertEquals(config, service.getConfig());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFactorySupportsDeprecatedMethods() throws Exception {
        DatastoreConfig oldConfig = DatastoreServiceFactory.getDefaultDatastoreConfig();
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService(oldConfig);
        assertTrue(datastoreService instanceof JBossDatastoreService);
    }

    @Test
    public void testDeadline() {
        for (int i=0; i<1000; i++) {
            buildTestEntity().withProperty("prop", i).store();
        }

        double ONE_MILLISECOND = 0.001;
        DatastoreService service = DatastoreServiceFactory.getDatastoreService(DatastoreServiceConfig.Builder.withDeadline(ONE_MILLISECOND));
        Query query = createQuery(GREATER_THAN, 0);

        long start = System.currentTimeMillis();
        List<Entity> entities = service.prepare(query).asList(withDefaults());
        try {
            entities.size();
            fail("Expected ApiProxy.ApiDeadlineExceededException");
        } catch (ApiProxy.ApiDeadlineExceededException e) {
            long duration = System.currentTimeMillis() - start;
            long MAX_DURATION = 100;    // 100 ms should be enough when the timeout is set to 1 ms
            assertTrue("ApiProxy.ApiDeadlineExceededException was thrown, but it was thrown too late (after " + duration + " ms)", duration < MAX_DURATION);
        }
    }


}
