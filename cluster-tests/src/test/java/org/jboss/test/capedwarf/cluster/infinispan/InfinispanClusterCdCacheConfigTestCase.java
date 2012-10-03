package org.jboss.test.capedwarf.cluster.infinispan;

import java.io.IOException;

import org.infinispan.Cache;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Infinispan cache configured at runtime by CapeDwarf.
 *
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class InfinispanClusterCdCacheConfigTestCase extends AbstractInfinispanClusterTest {

    @Override
    protected Cache<String, String> getCache() {
        return InfinispanUtils.getCache(CacheName.SEARCH);
    }

    @InSequence(10)
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
