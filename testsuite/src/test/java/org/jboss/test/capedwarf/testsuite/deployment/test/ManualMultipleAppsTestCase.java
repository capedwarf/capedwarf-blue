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

package org.jboss.test.capedwarf.testsuite.deployment.test;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class ManualMultipleAppsTestCase extends AbstractMultipleAppsTest {
    @Deployment (name = "depA", managed = false)
    public static WebArchive getDeploymentA() {
        return getDeployment("a");
    }

    @Deployment(name = "depB", managed = false)
    public static WebArchive getDeploymentB() {
        return getDeployment("b");
    }

    @ArquillianResource
    private Deployer deployer;

    @Test @InSequence(1)
    public void deployInitialApp() throws Exception {
        deployer.deploy("depA");
    }

    @Test @InSequence(2) @OperateOnDeployment("depA")
    public void testInitialApp() throws Exception {
        allTests(true);
    }

    @Test @InSequence(3)
    public void deployBundeployAdeployA() throws Exception {
        deployer.deploy("depB"); // deploy another, so we're still using ECM
        deployer.undeploy("depA");
        deployer.deploy("depA"); // re-deploy
    }

    @Test @InSequence(4) @OperateOnDeployment("depA")
    public void testInitialAppAgain() throws Exception {
        allTests(false);
    }
}
