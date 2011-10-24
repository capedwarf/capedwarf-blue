/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.memcache;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class MemcacheStatisticsTestCase {

    protected MemcacheService service;

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource("jboss/jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
    }

    @Before
    public void setUp() {
        service = MemcacheServiceFactory.getMemcacheService();
    }

    @After
    public void tearDown() {
        service.clearAll();
    }

    @Ignore("Must enable JMX statistics to get this to work")
    @Test
    public void testItemCount() {
        assertEquals(0, service.getStatistics().getItemCount());
        service.put("key1", "value1");
        assertEquals(1, service.getStatistics().getItemCount());
    }

    @Ignore("Must enable JMX statistics to get this to work")
    @Test
    public void testHitAndMissCount() {
        service.put("key1", "value1");

        assertEquals(0, service.getStatistics().getHitCount());
        assertEquals(0, service.getStatistics().getMissCount());

        service.get("key1");
        assertEquals(1, service.getStatistics().getHitCount());
        assertEquals(0, service.getStatistics().getMissCount());

        service.get("key2");
        assertEquals(1, service.getStatistics().getHitCount());
        assertEquals(1, service.getStatistics().getMissCount());
    }

}
