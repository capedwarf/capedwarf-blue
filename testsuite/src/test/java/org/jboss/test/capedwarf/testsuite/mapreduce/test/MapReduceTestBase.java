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

import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.LibUtils;
import org.jboss.test.capedwarf.testsuite.TestsuiteTestBase;
import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public abstract class MapReduceTestBase extends TestsuiteTestBase {

    protected static WebArchive getDefaultDeployment() {
        TestContext context = TestContext.asDefault();
        context.setContextRoot(true);
        context.setWebXmlFile("mapreduce/web.xml");
        context.setAppEngineWebXmlFile("mapreduce/appengine-web.xml");

        WebArchive war = getCapedwarfDeployment(context);
        war.addClass(TestsuiteTestBase.class);
        war.addClass(MapReduceTestBase.class);

        war.addAsWebInfResource("mapreduce/queue.xml", "queue.xml");
        war.addAsWebInfResource("mapreduce/logging.properties", "logging.properties");

        LibUtils.addGaeAsLibrary(war);
        LibUtils.addLibrary(war, "com.google.appengine.tools", "appengine-mapreduce");
        LibUtils.addLibrary(war, "com.google.appengine.tools", "appengine-mapreduce");
        LibUtils.addLibrary(war, "com.google.appengine.tools", "appengine-pipeline");

        // GCS
        LibUtils.addLibrary(war, "com.google.appengine.tools", "appengine-gcs-client");
        LibUtils.addLibrary(war, "joda-time", "joda-time");
        LibUtils.addLibrary(war, "com.google.api-client", "google-api-client");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client-appengine");
        LibUtils.addLibrary(war, "com.google.http-client", "google-http-client-jackson2");
        LibUtils.addLibrary(war, "com.google.api-client", "google-api-client-appengine");
        LibUtils.addLibrary(war, "com.google.apis", "google-api-services-storage");
        LibUtils.addLibrary(war, "com.fasterxml.jackson.core", "jackson-core");

        LibUtils.addLibrary(war, "com.google.guava", "guava");
        LibUtils.addLibrary(war, "it.unimi.dsi", "fastutil");
        LibUtils.addLibrary(war, "com.googlecode.charts4j", "charts4j");
        LibUtils.addLibrary(war, "commons-logging", "commons-logging");
        LibUtils.addLibrary(war, "org.json", "json");

        return war;
    }

    protected JobInfo getJobInfo(final String phase, final String handle) throws Exception {
        PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
        return getJobInfo(pipelineService, phase, handle);
    }

    protected JobInfo getJobInfo(PipelineService pipelineService, String phase, final String handle) throws Exception {
        JobInfo jobInfo = pipelineService.getJobInfo(handle);
        Assert.assertNotNull("Missing JobInfo - [ " + phase + " ] - handle: " + handle, jobInfo);
        return jobInfo;
    }

    protected JobInfo waitToFinish(final String phase, final String handle) throws Exception {
        PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
        JobInfo jobInfo = getJobInfo(pipelineService, phase, handle);
        JobInfo.State state = jobInfo.getJobState();
        int N = 24; // 2min
        while (isRunning(state) && N > 0) {
            N--;
            sync(5 * 1000L); // 5sec
            // new info lookup
            jobInfo = getJobInfo(pipelineService, phase, handle);
            state = jobInfo.getJobState();
        }
        if (N == 0 && isRunning(state)) {
            throw new IllegalStateException("Failed to finish the job [ " + phase + " ]: " + handle + ", info: " + toInfo(jobInfo));
        }
        if (state != JobInfo.State.COMPLETED_SUCCESSFULLY) {
            throw new IllegalStateException("Job " + handle + " failed [ " + phase + " ]: " + toInfo(jobInfo));
        }
        return jobInfo;
    }

    protected static String toInfo(JobInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("JobInfo[ ").append(info).append( "]");
        if (info != null) {
            sb.append(" --> ");
            sb.append("state - ").append(info.getJobState()).append(", ");
            sb.append("output - ").append(info.getOutput()).append(", ");
            sb.append("error - ").append(info.getError());
        }
        return sb.toString();
    }

    protected boolean isRunning(JobInfo.State state) {
        return (state == null || state == JobInfo.State.RUNNING);
    }

    protected MapReduceSettings getSettings() {
        return new MapReduceSettings().setWorkerQueueName("mapreduce-workers").setControllerQueueName("default");
    }
}
