/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.gql4j.test;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.gql4j.GqlQuery;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class GqlQueryTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment();
    }

    @Test
    public void testSelect() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        final Key key = ds.put(new Entity("_GQL_"));
        try {
            GqlQuery query = new GqlQuery("select * from _GQL_");
            List<Entity> results = ds.prepare(query.query()).asList(FetchOptions.Builder.withDefaults());
            Assert.assertEquals(1, results.size());
        } finally {
            ds.delete(key);
        }
    }

    @Test
    public void testWhere() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity("_GQL_");
        entity.setProperty("prop", 42L);
        final Key key = ds.put(entity);
        try {
            GqlQuery query = new GqlQuery("select * from _GQL_ where prop = 42");
            List<Entity> results = ds.prepare(query.query()).asList(FetchOptions.Builder.withDefaults());
            Assert.assertEquals(1, results.size());
        } finally {
            ds.delete(key);
        }
    }
}
