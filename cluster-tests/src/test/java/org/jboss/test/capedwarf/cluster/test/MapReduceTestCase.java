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

package org.jboss.test.capedwarf.cluster.test;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.mapreduce.Counter;
import com.google.appengine.tools.mapreduce.Counters;
import com.google.appengine.tools.mapreduce.KeyValue;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.inputs.ConsecutiveLongInput;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.InMemoryOutput;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.google.appengine.tools.mapreduce.reducers.NoReducer;
import com.google.appengine.tools.pipeline.JobInfo;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.CountMapper;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.CountReducer;
import org.jboss.test.capedwarf.testsuite.mapreduce.support.EntityCreator;
import org.jboss.test.capedwarf.testsuite.mapreduce.test.AbstractMapReduceTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * MapReduce cluster tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class MapReduceTestCase extends AbstractMapReduceTest {
    private List<String> payloads = Arrays.asList("capedwarf", "jboss", "redhat");

    public MapReduceTestCase() {
        ignoreTearDown = true;
    }

    @Deployment(name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getModuleDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getModuleDeployment();
    }

    protected static WebArchive getModuleDeployment() {
        LibUtils.applyTempModule("cluster-tests");
        try {
            return getDefaultDeployment().addPackage(EntityCreator.class.getPackage());
        } finally {
            LibUtils.applyTempModule(null);
        }
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(10)
    public void putDataInA() throws Exception {
        int shardCount = 1;
        final String createHandle = MapReduceJob.start(
                MapReduceSpecification.of(
                        "Create MapReduce entities",
                        new ConsecutiveLongInput(0, payloads.size() * (long) shardCount, shardCount),
                        new EntityCreator("MapReduceTest", payloads),
                        Marshallers.getVoidMarshaller(),
                        Marshallers.getVoidMarshaller(),
                        NoReducer.<Void, Void, Void>create(),
                        NoOutput.<Void, Void>create(1)),
                getSettings());

        Entity entity = new Entity("CMR", 1L);
        entity.setProperty("handle", createHandle);
        createDatastoreService().put(entity);

        sync();
    }

    @Test @OperateOnDeployment("dep2") @InSequence(20)
    public void waitForDataInB() throws Exception {
        DatastoreService service = createDatastoreService();
        Entity entity = service.get(KeyFactory.createKey("CMR", 1L));
        String createHandle = entity.getProperty("handle").toString();
        service.delete(entity.getKey()); // cleanup

        JobInfo createJI = waitToFinish("CREATE", createHandle);
        Object create = createJI.getOutput();
        log.severe("----- Create: " + create);
    }

    @Test
    @OperateOnDeployment("dep2") @InSequence(30)
    public void countDataInB() throws Exception {
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

        Entity entity = new Entity("CMR", 2);
        entity.setProperty("handle", countHandle);
        createDatastoreService().put(entity);

        sync();
    }

    @Test
    @OperateOnDeployment("dep1") @InSequence(40)
    public void waitForCountInA() throws Exception {
        Entity entity = createDatastoreService().get(KeyFactory.createKey("CMR", 2));
        String countHandle = entity.getProperty("handle").toString();

        JobInfo countJI = waitToFinish("COUNT", countHandle);
        Object count = countJI.getOutput();
        log.severe("----- Count: " + count);
    }

    @Test
    @OperateOnDeployment("dep2") @InSequence(50)
    public void verifyInB() throws Exception {
        DatastoreService service = createDatastoreService();
        Entity entity = service.get(KeyFactory.createKey("CMR", 2));
        String countHandle = entity.getProperty("handle").toString();
        service.delete(entity.getKey()); // cleanup

        JobInfo jobInfo = getJobInfo("VERIFY", countHandle);
        Assert.assertTrue("Not finished: " + toInfo(jobInfo), jobInfo.getJobState() == JobInfo.State.COMPLETED_SUCCESSFULLY);

        Object count = jobInfo.getOutput();
        Assert.assertTrue(count instanceof MapReduceResult);
        MapReduceResult result = MapReduceResult.class.cast(count);
        int[] chars = toChars(payloads);
        Counters counters = result.getCounters();
        for (int i = 0; i < chars.length; i++) {
            Counter c = counters.getCounter(CountMapper.toKey((char)('a' + i)));
            Assert.assertEquals(chars[i], c.getValue());
        }
    }

    @Test @OperateOnDeployment("dep1") @InSequence(60)
    public void cleanupOnA() throws Exception {
        cleanup();
    }

    protected static int[] toChars(List<String> payloads) {
        int[] chars = new int['z' - 'a' + 1];
        for (String payload : payloads) {
            for (int i = 0; i < payload.length(); i++) {
                chars[payload.charAt(i) - 'a']++;
            }
        }
        return chars;
    }
}
