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

import java.util.concurrent.atomic.AtomicLong;

import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class VersionTest extends DatastoreTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(TestContext.withMetadata());
    }

    @Test
    public void testIncrement() throws Exception {
        Entity entity = new Entity("VersionTest");
        try {
            // 3 puts
            AtomicLong counter = new AtomicLong(1);
            assertVersion(entity, counter);
            assertVersion(entity, counter);
            assertVersion(entity, counter);
        } finally {
            // cleanup
            service.delete(entity.getKey());
        }
    }

    protected void assertVersion(Entity entity, AtomicLong counter) throws Exception {
        entity.setProperty("counter", counter.get());

        Key key = service.put(entity);
        entity = service.get(key);

        Assert.assertEquals(counter.get(), entity.getProperty("counter"));

        long actualVersion = Entities.getVersionProperty(service.get(Entities.createEntityGroupKey(key)));
        Assert.assertEquals(counter.getAndIncrement(), actualVersion);
    }
}
