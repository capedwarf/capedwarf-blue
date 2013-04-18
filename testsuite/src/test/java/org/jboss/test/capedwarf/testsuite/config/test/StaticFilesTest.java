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

package org.jboss.test.capedwarf.testsuite.config.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.config.support.PingFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class StaticFilesTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = TestContext.asDefault();
        context.setWebXmlFile("static/web.xml");
        context.setAppEngineWebXmlFile("static/appengine-web.xml");
        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(PingFilter.class);
        war.add(new ByteArrayAsset("12345".getBytes()), "image.png");
        return war;
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void applyPing(@ArquillianResource URL url) throws Exception {
        URL imageURL = new URL(url, "image.png");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream stream = imageURL.openStream();
        try {
            IOUtils.copyStream(stream, baos);
        } finally {
            IOUtils.safeClose(stream);
        }
        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    @InSequence(20)
    public void checkPing() {
        Assert.assertEquals(1, PingFilter.state.size());
    }
}
