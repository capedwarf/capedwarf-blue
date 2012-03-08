package org.jboss.test.capedwarf.cluster;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class AbstractClusteredTest {

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(AbstractClusteredTest.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml");
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(AbstractClusteredTest.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml");
    }


}
