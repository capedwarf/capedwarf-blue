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

import java.net.URL;

import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BackendsTestCase extends BaseTest {

    public static final String INDEX_HTML_CONTENT = "hello";

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addAsWebInfResource("backends.xml");
        war.addAsWebResource(new StringAsset(INDEX_HTML_CONTENT), "index.html");
        return war;
    }

    @Test
    public void testBackendServesTheSameWarAsFrontend() throws Exception {
        BackendService bs = BackendServiceFactory.getBackendService();
        String address = bs.getBackendAddress("backend");
        assertNotNull(address);

        HTTPResponse response = URLFetchServiceFactory.getURLFetchService().fetch(new URL("http://" + address + "/index.html"));
        assertEquals(INDEX_HTML_CONTENT, new String(response.getContent()));
    }
}
