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

package org.jboss.test.capedwarf.testsuite.ext.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Mock Iddx deployment.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class IddxTestCase extends BaseTest {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        LibUtils.addLibrary(war, "com.google.api-client", "google-api-client");
        LibUtils.addLibrary(war, "com.google.api-client", "google-api-client-appengine");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client-appengine");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client-jackson");
        LibUtils.addLibrary(war, "com.google.oauth-client", "google-oauth-client");
        LibUtils.addLibrary(war, "com.google.oauth-client", "google-oauth-client-appengine");
        LibUtils.addLibrary(war, "com.google.oauth-client", "google-oauth-client-servlet");
        // TODO -- enable this once AS7-6281 is fixed
        // LibUtils.addLibrary(war, "com.google.apis", "google-api-services-bigquery");
        return war;
    }

    @Test
    public void testPing() {
        // do nothing, it's deployment check
    }
}

