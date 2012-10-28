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

package org.jboss.test.capedwarf.appidentity;

import java.util.Map;

import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class ApiProxyTestCase extends BaseTest {

    @Deployment
    public static Archive getDeployment() {
        return getCapedwarfDeployment();
    }

    @Test
    public void testCurrentEnvironmentIsNotNull() {
        ApiProxy.Environment currentEnvironment = ApiProxy.getCurrentEnvironment();
        assertNotNull(currentEnvironment);
    }

    @Test
    public void testCurrentEnvironmentRemembersAttributes() {
        ApiProxy.getCurrentEnvironment().getAttributes().put("attributeName", "attributeValue");

        Map<String, Object> attributes = ApiProxy.getCurrentEnvironment().getAttributes();
        assertTrue(attributes.containsKey("attributeName"));
        assertEquals("attributeValue", attributes.get("attributeName"));
    }
}
