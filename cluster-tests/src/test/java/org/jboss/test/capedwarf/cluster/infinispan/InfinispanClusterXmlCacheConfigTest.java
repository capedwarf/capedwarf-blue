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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class InfinispanClusterXmlCacheConfigTest extends AbstractInfinispanClusterTest {

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
        return getBaseDeployment();
    }
}
