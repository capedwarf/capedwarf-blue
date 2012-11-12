/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.capedwarf.testsuite.mapreduce.test;

import java.util.logging.Logger;

import com.google.appengine.tools.mapreduce.KeyValue;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.inputs.ConsecutiveLongInput;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.InMemoryOutput;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.google.appengine.tools.mapreduce.reducers.NoReducer;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.AbstractTest;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.CountMapper;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.CountReducer;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.EntityCreator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MapReduceTestCase extends AbstractTest {
    private Logger log = Logger.getLogger(MapReduceTestCase.class.getName());

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("mapreduce/web.xml");
        context.setAppEngineWebXmlFile("mapreduce/appengine-web.xml");

        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(AbstractTest.class);

        war.addAsWebInfResource("mapreduce/queue.xml", "queue.xml");
        war.addAsWebInfResource("mapreduce/logging.properties", "logging.properties");

        war.addPackage(EntityCreator.class.getPackage());

        LibUtils.addGaeAsLibrary(war);
        LibUtils.addLibrary(war, "com.google.appengine", "appengine-mapper");
        LibUtils.addLibrary(war, "com.google.guava", "guava");
        LibUtils.addLibrary(war, "com.googlecode.charts4j", "charts4j");
        LibUtils.addLibrary(war, "commons-logging", "commons-logging");
        LibUtils.addLibrary(war, "org.apache.hadoop", "hadoop-core");
        LibUtils.addLibrary(war, "org.json", "json");

        return war;
    }

    @Test
    @Ignore
    public void testCountJob() throws Exception {
        int bytesPerEntity = 10;
        int entitiesPerShard = 1;
        int shardCount = 1;

        final String createHandle = MapReduceJob.start(
                MapReduceSpecification.of(
                        "Create MapReduce entities",
                        new ConsecutiveLongInput(0, entitiesPerShard * (long) shardCount, shardCount),
                        new EntityCreator("MapReduceTest", bytesPerEntity),
                        Marshallers.getVoidMarshaller(),
                        Marshallers.getVoidMarshaller(),
                        NoReducer.<Void, Void, Void>create(),
                        NoOutput.<Void, Void>create(1)),
                        getSettings());

        JobInfo createJI = waitToFinish("CREATE", createHandle);
        Object create = createJI.getOutput();
        log.warning("----- Create: " + create);

        int mapShardCount = 1;
        int reduceShardCount = 1;

        String countHandle = MapReduceJob.start(
                MapReduceSpecification.of(
                        "MapReduceTest stats",
                        new DatastoreInput("MapReduceTest", mapShardCount),
                        new CountMapper(),
                        Marshallers.getStringMarshaller(),
                        Marshallers.getLongMarshaller(),
                        new CountReducer(),
                        new InMemoryOutput<KeyValue<String, Long>>(reduceShardCount)),
                getSettings());

        JobInfo countJI = waitToFinish("COUNT", countHandle);
        Object count = countJI.getOutput();
        log.warning("----- Count: " + count);
    }

    protected JobInfo waitToFinish(final String phase, final String handle) throws Exception {
        PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
        JobInfo jobInfo = pipelineService.getJobInfo(handle);
        JobInfo.State state = jobInfo.getJobState();
        int N = 10;
        while (isRunning(state) && N > 0) {
            N--;
            Thread.sleep(30 * 1000L); // 30sec
            // new info lookup
            jobInfo = pipelineService.getJobInfo(handle);
            state = jobInfo.getJobState();
        }
        if (N == 0 && isRunning(state)) {
            throw new IllegalStateException("Failed to finish the job [ " + phase + " ]: " + handle);
        }
        if (state != JobInfo.State.COMPLETED_SUCCESSFULLY) {
            throw new IllegalStateException("Job " + handle + " failed [ " + phase + " ]: " + jobInfo);
        }
        return jobInfo;
    }

    protected boolean isRunning(JobInfo.State state) {
        return (state == null || state == JobInfo.State.RUNNING);
    }

    protected MapReduceSettings getSettings() {
        return new MapReduceSettings().setWorkerQueueName("mapreduce-workers").setControllerQueueName("default");
    }
}
