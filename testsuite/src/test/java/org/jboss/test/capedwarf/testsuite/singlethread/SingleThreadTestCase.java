package org.jboss.test.capedwarf.testsuite.singlethread;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class SingleThreadTestCase extends BaseTest {

    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment(TestContext.asDefault().setAppEngineWebXmlFile("appengine-web-no-threadsafe-tag.xml"));
    }

    @Test
    public void testDeployment() {
    }

}
