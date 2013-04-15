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

import java.util.Properties;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class CompatibilityUsageTest extends DatastoreTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        Properties properties = context.getProperties();
        properties.setProperty("disable.entity.groups", Boolean.TRUE.toString());
        properties.setProperty("ignore.entity.property.conversion", Boolean.TRUE.toString());
        return getDefaultDeployment(context);
    }

    @Test
    public void testDisableEntityGroups() throws Exception {
        Key k1 = service.put(new Entity("Cmpt1"));
        Key k2 = service.put(new Entity("Cmpt2"));
        try {
            Transaction tx = service.beginTransaction();
            try {
                // two diff kinds == two diff entity groups, should fail normally
                service.get(k1);
                service.get(k2);
            } finally {
                tx.rollback();
            }
        } finally {
            service.delete(k1, k2);
        }
    }

    @Test
    public void testIgnoredEntityPropertyConversion() throws Exception {
        Entity e1 = new Entity("IEPC");
        int x = 123;
        e1.setProperty("x", x);
        float f = 3.14f;
        e1.setProperty("f", f);
        Key k1 = service.put(e1);
        try {
            Entity e2 = service.get(k1);
            Object px = e2.getProperty("x");
            // we put in integer, so we expect one back, unmodified
            Assert.assertTrue(px instanceof Integer);
            Object pf = e2.getProperty("f");
            // we put in float, so we expect one back, unmodified
            Assert.assertTrue(pf instanceof Float);
        } finally {
            service.delete(k1);
        }
    }

}
