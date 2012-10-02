package org.jboss.test.capedwarf.cluster.infinispan;

import java.io.IOException;

import javax.annotation.Resource;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Infinispan cache configuration used by CapeDwarf.
 *
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class InfinispanClusterXmlCacheConfigTestCase extends AbstractInfinispanClusterTest {

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

    /**
     * Wake-up cache on second node.
     */
    @InSequence(10)
    @Test
    @OperateOnDeployment("dep2")
    public void wakeUpCache() throws IOException {
        getCache();
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep1")
    public void wordCountTestCase() throws IOException {
        wordCount();
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void cleanUp() throws IOException {
        getCache().clear();
    }

}
