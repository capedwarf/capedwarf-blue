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

package org.jboss.test.capedwarf.tasks.test;

import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.tasks.support.PrintListener;
import org.jboss.test.capedwarf.tasks.support.PrintServlet;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class TasksTestCase {
    private static final String URL = "/_ah/test";
    private static final String WEB_XML =
        "<web>" +
            " <listener>" +
            "  <listener-class>" + PrintListener.class.getName() + "</listener-class>" +
            " </listener>" +
            " <servlet>" +
            "  <servlet-name>PrintServlet</servlet-name>" +
            "  <servlet-class>" + PrintServlet.class.getName() + "</servlet-class>" +
            " </servlet>" +
            " <servlet-mapping>" +
            "  <servlet-name>PrintServlet</servlet-name>" +
            "  <url-pattern>" + URL + "</url-pattern>" +
            " </servlet-mapping>" +
            "</web>";

    // we wait for JMS to kick-in
    private static void sleep() throws InterruptedException {
        Thread.sleep(3000L); // sleep for 3secs
    }

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(PrintServlet.class, PrintListener.class)
            .setWebXML(new StringAsset(WEB_XML))
            .addAsWebInfResource("appengine-web.xml")
            .addAsWebInfResource("queue-tasks.xml", "queue.xml");
    }

    @After
    public void tearDown() throws Exception {
        PrintServlet.reset();
    }

    @Test
    public void testSmoke() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(TaskOptions.Builder.withUrl(URL));
        sleep();
        assertNotNull(PrintServlet.getLastRequest());
    }

    @Test
    public void testPayload() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(TaskOptions.Builder.withPayload("payload").url(URL));
        sleep();
    }

    @Test
    public void testHeaders() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(TaskOptions.Builder.withHeader("header_key", "header_value").url(URL));
        sleep();

        HttpServletRequest request = (HttpServletRequest) PrintServlet.getLastRequest();
        assertEquals("header_value", request.getHeader("header_key"));
    }

    @Test
    public void testParams() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(TaskOptions.Builder.withParam("single_value", "param_value").url(URL));
        sleep();

        ServletRequest request = PrintServlet.getLastRequest();
        assertEquals("param_value", request.getParameter("single_value"));
    }

    @Test
    public void testMultiValueParams() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(
            TaskOptions.Builder
                .withParam("multi_value", "param_value1")
                .param("multi_value", "param_value2")
                .url(URL));
        sleep();

        ServletRequest request = PrintServlet.getLastRequest();
        String[] multi_values = request.getParameterValues("multi_value");
        assertNotNull(multi_values);
        assertEquals(
            new HashSet<String>(Arrays.asList("param_value1", "param_value2")),
            new HashSet<String>(Arrays.asList(multi_values)));
    }
}
