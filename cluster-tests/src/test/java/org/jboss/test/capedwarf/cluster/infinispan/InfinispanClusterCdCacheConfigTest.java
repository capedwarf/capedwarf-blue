package org.jboss.test.capedwarf.cluster.infinispan;

import java.io.IOException;

import org.infinispan.Cache;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class InfinispanClusterCdCacheConfigTest extends AbstractInfinispanClusterTest {

    @Override
    protected Cache<String, String> getCache() {
        //ClassLoader classLoader = Application.getAppClassloader();
        //TODO use this ? Cache<String, String> cache = InfinispanUtils.<String, String>getCache(CacheName.DEFAULT).getAdvancedCache().with(classLoader);
        Cache<String, String> cache = InfinispanUtils.<String, String>getCache(CacheName.SEARCH);
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
