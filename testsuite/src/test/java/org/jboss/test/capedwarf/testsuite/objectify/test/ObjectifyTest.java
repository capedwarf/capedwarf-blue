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

package org.jboss.test.capedwarf.testsuite.objectify.test;

import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.TestsuiteTestBase;
import org.jboss.test.capedwarf.testsuite.objectify.support.Car;
import org.jboss.test.capedwarf.testsuite.objectify.support.Snapshot;
import org.jboss.test.capedwarf.testsuite.objectify.support.TestEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class ObjectifyTest extends TestsuiteTestBase {
    private Objectify objectify;

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(TestsuiteTestBase.class);
        war.addPackage(Car.class.getPackage());
        LibUtils.addObjectifyLibrary(war);
        return war;
    }

    @Before
    public void setUp() {
        ObjectifyService.register(Car.class);
        ObjectifyService.register(Snapshot.class);
        ObjectifyService.register(TestEntity.class);

        objectify = ObjectifyService.ofy();
    }

    @After
    public void tearDown() {
        ObjectifyService.reset();
    }

    @Test
    public void testSmoke() throws Exception {
        Car c1 = new Car();
        c1.setMark("Mazda");
        c1.setType("CX-5");

        Map<Key<Car>, Car> keys = objectify.save().entities(c1).now();
        Assert.assertEquals(1, keys.size());
        final Key<Car> key = keys.keySet().iterator().next();
        try {
            Car c2 = objectify.load().key(key).get();

            Assert.assertEquals(c1.getMark(), c2.getMark());
            Assert.assertEquals(c1.getType(), c2.getType());
        } finally {
            objectify.delete().key(key).now();
        }
    }

    @Test
    public void testCD123() throws Exception {
        long start = 0;
        long end = 1000;

        Snapshot s1 = new Snapshot();
        s1.setTimestamp(500);
        objectify.save().entities(s1).now();

        List<Snapshot> list = objectify.load()
                .type(Snapshot.class)
                .filter("timestamp >=", start)
                .filter("timestamp <", end)
                .list();

        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testWithCache() throws Exception {
        System.out.println("Current: " + ObjectifyService.ofy().load().type(TestEntity.class).list());

        final TestEntity entity = new TestEntity().setName("TESTING 1");

        //simple save
        ObjectifyService.ofy().transact(new VoidWork() {
            public void vrun() {
                ObjectifyService.ofy().save().entity(entity).now();
            }
        });

        //get and save again
        ObjectifyService.ofy().transact(new VoidWork() {
            public void vrun() {
                TestEntity entityBack = ObjectifyService.ofy().load().type(TestEntity.class).id(entity.getId()).get();
                ObjectifyService.ofy().save().entity(entityBack.setName("TESTING 2"));
            }
        });
    }
}
