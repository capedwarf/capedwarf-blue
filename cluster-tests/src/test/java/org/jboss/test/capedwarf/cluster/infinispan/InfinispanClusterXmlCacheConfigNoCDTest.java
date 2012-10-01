package org.jboss.test.capedwarf.cluster.infinispan;

import java.io.IOException;

import javax.annotation.Resource;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test is meant to run on AS without Capedwarf.
 * AS must be run with standalone-capedwarf.xml configuration, with capedwarf module removed.
 * Cache search requires eager start option.
 *
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class InfinispanClusterXmlCacheConfigNoCDTest extends AbstractInfinispanClusterTest {

    @Resource(lookup="java:jboss/infinispan/container/capedwarf")
    CacheContainer container;

    private Cache<String, String> cache;

    @Override
    protected Cache<String, String> getCache() {
        if (cache == null) {
            cache = container.getCache("search");
        }
        return cache;
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void wordCountTestCase() throws IOException {
        wordCount();
    }

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        return getBaseDeployment()
            .addAsManifestResource(new StringAsset("Dependencies: org.infinispan export\n"),"MANIFEST.MF")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
