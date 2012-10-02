package org.jboss.test.capedwarf.cluster.infinispan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map.Entry;

import org.infinispan.Cache;
import org.infinispan.demo.mapreduce.WordCountCollator;
import org.infinispan.demo.mapreduce.WordCountMapper;
import org.infinispan.demo.mapreduce.WordCountReducer;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.infinispan.util.Util;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Matej Lazar
 */
abstract public class AbstractInfinispanClusterTest  {

    private final String textFileName = "dummy.txt";
    private final int numPopularWords = 20;

    protected void wordCount() throws IOException {
        Cache<String, String> cache = getCache();

        loadData(cache);

        MapReduceTask<String, String, String, Integer> mapReduceTask = new MapReduceTask<String, String, String, Integer>(cache);

        List<Entry<String, Integer>> topList =
              mapReduceTask
                    .mappedWith(new WordCountMapper())
                    .reducedWith(new WordCountReducer())
                    .execute(new WordCountCollator(numPopularWords));

        System.out.printf(" ---- RESULTS: Top %s words in %s ---- %n%n", numPopularWords, textFileName);
        int z = 0;
        for (Entry<String, Integer> e : topList) {
            System.out.printf("  %s) %s [ %,d occurences ]%n", ++z, e.getKey(), e.getValue());
        }
    }

    abstract protected Cache<String, String> getCache();

    private void loadData(Cache<String, String> cache) throws IOException {
        //

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(textFileName);
        InputStreamReader reader = new InputStreamReader(in);
        try {

           BufferedReader bufferedReader = new BufferedReader(reader);

           //chunk and insert into cache
           int chunkSize = 10; // 10K
           int chunkId = 0;

           CharBuffer cbuf = CharBuffer.allocate(1024 * chunkSize);
           while (bufferedReader.read(cbuf) >= 0) {
              Buffer buffer = cbuf.flip();
              String textChunk = buffer.toString();
              cache.put(textFileName + (chunkId++), textChunk);
              cbuf.clear();
              if (chunkId % 100 == 0) System.out.printf("  Inserted %s chunks from %s into grid%n", chunkId, textFileName);
           }
        } finally {
           Util.close(reader);
           Util.close(in);
        }
     }

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    protected static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, "cluster-tests.war")
            .addClass(AbstractInfinispanClusterTest.class)
            .addClass(WordCountMapper.class)
            .addClass(WordCountReducer.class)
            .addClass(WordCountCollator.class)
            .setWebXML(new StringAsset("<web/>"))
            .addAsWebInfResource("appengine-web.xml")
            .addAsResource("dummy.txt");
    }
}
