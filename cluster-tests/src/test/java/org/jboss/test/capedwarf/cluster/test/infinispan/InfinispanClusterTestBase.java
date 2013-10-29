package org.jboss.test.capedwarf.cluster.test.infinispan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;

/**
 * @author Matej Lazar
 */
public abstract class InfinispanClusterTestBase extends TestBase {

    private final String textFileName = "dummy.txt";
    private final int numPopularWords = 20;

    protected void wordCount() throws IOException {
        Cache<String, String> cache = getCache();

        loadData(cache);

        MapReduceTask<String, String, String, Integer> mapReduceTask = new MapReduceTask<String, String, String, Integer>(cache);

        List<Entry<String, Integer>> topList = new ArrayList<>();

        // TODO, FIXME -- port over mapper classes from demo!

        /*
              mapReduceTask
                    .mappedWith(new WordCountMapper())
                    .reducedWith(new WordCountReducer())
                    .execute(new WordCountCollator(numPopularWords));
        */
        System.out.printf(" ---- RESULTS: Top %s words in %s ---- %n%n", numPopularWords, textFileName);
        int z = 0;
        for (Entry<String, Integer> e : topList) {
            System.out.printf("  %s) %s [ %,d occurences ]%n", ++z, e.getKey(), e.getValue());
        }
    }

    abstract protected Cache<String, String> getCache();

    private void loadData(Cache<String, String> cache) throws IOException {
        //

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(textFileName)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            //chunk and insert into cache
            int chunkSize = 10; // 10K
            int chunkId = 0;

            CharBuffer cbuf = CharBuffer.allocate(1024 * chunkSize);
            while (bufferedReader.read(cbuf) >= 0) {
                Buffer buffer = cbuf.flip();
                String textChunk = buffer.toString();
                cache.put(textFileName + (chunkId++), textChunk);
                cbuf.clear();
                if (chunkId % 100 == 0)
                    System.out.printf("  Inserted %s chunks from %s into grid%n", chunkId, textFileName);
            }
        }
    }

    @Deployment(name = "dep1")
    @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2")
    @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    protected static WebArchive getDeployment() {
        final WebArchive war = getCapedwarfDeployment();
        war.addClass(InfinispanClusterTestBase.class);
        /*
        war.addClass(WordCountMapper.class);
        war.addClass(WordCountReducer.class);
        war.addClass(WordCountCollator.class);
        */
        war.addAsResource("dummy.txt");
        return war;
    }
}
