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

package org.jboss.capedwarf.common.config;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfConfigurationTestCase {

    private CapedwarfConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new CapedwarfConfiguration();
    }

    @Test
    public void isAdminReturnsTrueForAllAddedAdmins() throws Exception {
        config.addAdmin("admin@email.com");
        config.addAdmin("admin2@email.com");
        assertTrue(config.isAdmin("admin@email.com"));
        assertTrue(config.isAdmin("admin2@email.com"));
    }

    @Test
    public void letterCaseIsIgnored() throws Exception {
        config.addAdmin("ADMIN@email.com");
        assertTrue(config.isAdmin("admin@email.com"));
        assertTrue(config.isAdmin("admin@EMAIL.COM"));
        assertTrue(config.isAdmin("AdMiN@EmAiL.CoM"));
    }

    @Test
    public void isAdminReturnsFalseForNotAddedAdmins() throws Exception {
        config.addAdmin("admin@email.com");
        assertFalse(config.isAdmin("notadmin@email.com"));
    }

    @Test
    public void getAdminsReturnsAllAddedAdmins() throws Exception {
        config.addAdmin("admin@email.com");
        config.addAdmin("admin2@email.com");
        assertEquals(2, config.getAdmins().size());
        assertTrue(config.getAdmins().contains("admin@email.com"));
        assertTrue(config.getAdmins().contains("admin2@email.com"));
    }

}
