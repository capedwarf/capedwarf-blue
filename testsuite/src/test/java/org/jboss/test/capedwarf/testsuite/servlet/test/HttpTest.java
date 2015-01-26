/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.testsuite.servlet.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.servlet.support.SimpleServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class HttpTest extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        context.setAppEngineWebXmlFile("servlet/appengine-web.xml");
        context.setWebXmlFile("servlet/web.xml");
        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(SimpleServlet.class);
        return war;
    }

    @RunAsClient
    @Test
    public void testGet(@ArquillianResource URL url) throws Exception {
        URL testURL = new URL(url, "simple");
        HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.connect();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = conn.getInputStream()) {
                IOUtils.copyStream(is, baos);
            }
            Assert.assertTrue(baos.toString().contains("OK"));
        } finally {
            conn.disconnect();
        }
    }
}
