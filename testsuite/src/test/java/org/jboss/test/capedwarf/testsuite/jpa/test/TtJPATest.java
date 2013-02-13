package org.jboss.test.capedwarf.testsuite.jpa.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Mock Tattletale JPA usage.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class TtJPATest extends SimpleJPATestBase {

    @Deployment
    public static WebArchive getDeployment() {
        final WebArchive war = getBaseDeployment();
        TestUtils.addPersistenceXml(war, "jpa/tt-persistence.xml");
        TestUtils.addLibraries(war);
        return war;
    }

}
