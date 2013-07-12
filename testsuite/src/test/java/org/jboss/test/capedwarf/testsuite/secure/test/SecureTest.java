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

package org.jboss.test.capedwarf.testsuite.secure.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.Appspot;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.secure.support.SecureServlet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category({JBoss.class, Appspot.class})
public class SecureTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        context.setWebXmlFile("secure/web.xml");
        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(SecureServlet.class);
        LibUtils.addGaeAsLibrary(war);
        return war;
    }

    @Test
    @RunAsClient
    public void testSecurePing(@ArquillianResource URL url) throws Exception {
        URLConnection uc = new URL(url, "secure").openConnection();
        HttpURLConnection conn = (HttpURLConnection) uc;
        conn.connect();
        try {
            conn.getInputStream();
            Assert.fail("Should not be here!");
        } catch (IOException e) {
            int responseCode = conn.getResponseCode();
            Assert.assertTrue(400 <= responseCode && responseCode < 500); // OK?
        } finally {
            conn.disconnect();
        }
    }

    @Test
    @RunAsClient
    @Ignore
    public void testAdmin(@ArquillianResource URL url) throws Exception {
        URLConnection uc = new URL(url, "_ah/admin").openConnection();
        HttpURLConnection conn = (HttpURLConnection) uc;
        conn.connect();
        conn.getInputStream().read();
        int responseCode = conn.getResponseCode();
//        Assert.assertEquals(401, responseCode);
    }
}
