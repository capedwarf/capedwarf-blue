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

package org.jboss.test.capedwarf.testsuite.env.test;

import java.util.Arrays;

import com.google.appengine.api.utils.SystemProperty;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Production;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class EnvironmentTest extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        LibUtils.addGaeAsLibrary(war);
        return war;
    }

    @Test
    public void testEnvironmentIsSet() {
        assertTrue("SystemProperty.environment.value() should either return Development or Production",
                Arrays.asList(Development, Production).contains(SystemProperty.environment.value()));
        assertTrue("System.getProperty(c.g.a.runtime.environment) should either return Development or Production",
            Arrays.asList(Development.name(), Production.name()).contains(System.getProperty(SystemProperty.environment.key())));
    }

    @Test
    public void testVersionIsSet() {
        assertNotNull(SystemProperty.version.get());
        assertNotNull(System.getProperty(SystemProperty.version.key()));
        assertTrue(SystemProperty.version.get().equals(System.getProperty(SystemProperty.version.key())));
    }

    @Test
    public void testApplicationIdIsSet() {
        assertEquals("capedwarf-test", SystemProperty.applicationId.get());
        assertEquals("capedwarf-test", System.getProperty(SystemProperty.applicationId.key()));
    }

    @Test
    public void testApplicationVersionIsSet() {
        assertRegexpMatches("1\\.[0-9]+", SystemProperty.applicationVersion.get());
        assertRegexpMatches("1\\.[0-9]+", System.getProperty(SystemProperty.applicationVersion.key()));
    }

}
