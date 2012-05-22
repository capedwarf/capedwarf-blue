package org.jboss.test.capedwarf.testsuite.jpa.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class BundledJPATestCase extends SimpleJPATest {

    @Deployment
    public static WebArchive getDeployment() {
        final WebArchive war = getDefaultDeployment();
        war.addClass(BundledJPATestCase.class);
        TestUtils.addLibraries(war);
        return war;
    }

}
