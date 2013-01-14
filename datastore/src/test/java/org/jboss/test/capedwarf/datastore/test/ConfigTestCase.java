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

import java.util.List;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.BaseDatastoreService;
import com.google.appengine.api.datastore.DatastoreConfig;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class ConfigTestCase extends QueryTest {

    @Test
    public void testFactoryPassesConfigToDatastoreServiceInstance() throws Exception {
        if (isRunningInsideGaeDevServer()) {
            return;
        }
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
        DatastoreService service = DatastoreServiceFactory.getDatastoreService(config);
        assertEquals(config, getConfig(service));
    }

    @Test
    public void testFactoryPassesConfigToAsyncDatastoreServiceInstance() throws Exception {
        if (isRunningInsideGaeDevServer()) {
            return;
        }
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService(config);
        assertEquals(config, getConfig(service));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFactorySupportsDeprecatedMethods() throws Exception {
        if (isRunningInsideGaeDevServer()) {
            return;
        }
        DatastoreConfig oldConfig = DatastoreServiceFactory.getDefaultDatastoreConfig();
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService(oldConfig);
        assertTrue(isJBossDatastoreService(datastoreService));
    }

    private DatastoreServiceConfig getConfig(BaseDatastoreService service) {
        assertTrue(isJBossDatastoreService(service));
        return (DatastoreServiceConfig) ReflectionUtils.invokeInstanceMethod(service, "getDatastoreServiceConfig", new Class[0], new Object[0]);
    }

    private boolean isJBossDatastoreService(BaseDatastoreService service) {
        return service.getClass().getSimpleName().equals("CapedwarfDatastoreService")
            || service.getClass().getSimpleName().equals("CapedwarfAsyncDatastoreService");
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
            long MAX_DURATION = 500;    // 500 ms should be enough when the timeout is set to 1 ms
            assertTrue("ApiProxy.ApiDeadlineExceededException was thrown, but it was thrown too late (after " + duration + " ms)", duration < MAX_DURATION);
        }
    }


}
