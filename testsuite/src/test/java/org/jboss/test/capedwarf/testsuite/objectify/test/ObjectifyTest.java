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

import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.TestsuiteTestBase;
import org.jboss.test.capedwarf.testsuite.objectify.support.Car;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class ObjectifyTest extends TestsuiteTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(TestsuiteTestBase.class);
        war.addPackage(Car.class.getPackage());
        LibUtils.addObjectifyLibrary(war);
        return war;
    }

    @Test
    public void testSmoke() throws Exception {
        ObjectifyService.register(Car.class);
        ObjectifyFactory factory = ObjectifyService.factory();
        Objectify objectify = factory.begin();

        Car c1 = new Car();
        c1.setMark("Mazda");
        c1.setType("CX-5");

        Map<Key<Car>, Car> keys = objectify.save().entities(c1).now();
        Assert.assertEquals(1, keys.size());

        Car c2 = objectify.load().key(keys.keySet().iterator().next()).get();

        Assert.assertEquals(c1.getMark(), c2.getMark());
        Assert.assertEquals(c1.getType(), c2.getType());
    }
}
