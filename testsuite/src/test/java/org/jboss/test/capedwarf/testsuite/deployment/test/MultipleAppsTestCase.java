package org.jboss.test.capedwarf.testsuite.deployment.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MultipleAppsTestCase extends AbstractMultipleAppsTest {
    @Deployment (name = "depA")
    public static WebArchive getDeploymentA() {
        return getDeployment("a");
    }

    @Deployment(name = "depB")
    public static WebArchive getDeploymentB() {
        return getDeployment("b");
    }

    @Test @OperateOnDeployment("depA")
    public void depATests() throws Exception {
        allTests();
    }

    @Test @OperateOnDeployment("depB")
    public void depBTests() throws Exception {
        allTests();
    }
}
