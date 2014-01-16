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

package org.jboss.test.capedwarf.testsuite.config.test;

import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.BackendsXml;
import org.jboss.capedwarf.shared.config.CapedwarfConfiguration;
import org.jboss.capedwarf.shared.config.IndexesXml;
import org.jboss.capedwarf.shared.config.QueueXml;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@Category(JBoss.class)
public class CapedwarfEnvironmentTest {

    private static final String USER_EMAIL = "user@email.com";
    private static final String ADMIN_EMAIL = "admin@email.com";

    private CapedwarfConfiguration config;
    private CapedwarfEnvironment env;

    @Before
    public void setUp() throws Exception {
        config = new CapedwarfConfiguration();
        env = CapedwarfEnvironment.createThreadLocalInstance();
        env.setApplicationConfiguration(new ApplicationConfiguration(null, config, new QueueXml(), new BackendsXml(), new IndexesXml()));
    }

    @After
    public void tearDown() throws Exception {
        CapedwarfEnvironment.clearThreadLocalInstance();
    }

    @Test
    public void getEmail_ReturnsSetEmail() throws Exception {
        env.setEmail(USER_EMAIL);
        Assert.assertEquals(USER_EMAIL, env.getEmail());
    }

    @Test
    public void isLoggedIn_InitiallyReturnsFalse() throws Exception {
        assertFalse(env.isLoggedIn());
    }

    @Test
    public void isLoggedIn_ReturnsTrueWhenEmailSet() throws Exception {
        env.setEmail(USER_EMAIL);
        assertTrue(env.isLoggedIn());
    }

    @Test
    public void isAdmin_ReturnsFalseWhenLoggedInUserNotAdmin() throws Exception {
        config.addAdmin(ADMIN_EMAIL);
        env.setEmail(USER_EMAIL);
        env.setAdmin(false); // pretty useless?
        assertFalse(env.isAdmin());
    }

    @Test
    public void isAdmin_ReturnsTrueWhenLoggedInUserIsAdmin() throws Exception {
        config.addAdmin(ADMIN_EMAIL);
        env.setEmail(ADMIN_EMAIL);
        env.setAdmin(true); // pretty useless?
        assertTrue(env.isAdmin());
    }

    @Test
    public void attributesContainDefaultVersionHostname() throws Exception {
        String HOSTNAME = "myapp.capedwarf.com";
        env.setBaseApplicationUrl("http", "myapp.capedwarf.com", 80, "");
        Assert.assertEquals(HOSTNAME, env.getAttributes().get("com.google.appengine.runtime.default_version_hostname"));
    }

}
