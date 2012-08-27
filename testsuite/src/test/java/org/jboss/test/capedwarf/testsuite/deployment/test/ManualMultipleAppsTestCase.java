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
        allTests();
    }

    @Test @InSequence(3)
    public void deployBundeployAdeployA() throws Exception {
        deployer.deploy("depB"); // deploy another, so we're still using ECM
        deployer.undeploy("depA");
        deployer.deploy("depA"); // re-deploy
    }

    @Test @InSequence(4) @OperateOnDeployment("depA")
    public void testInitialAppAgain() throws Exception {
        allTests();
    }
}
