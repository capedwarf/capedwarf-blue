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

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class PullTestCase {
    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .setWebXML(new StringAsset("<web/>"))
                .addAsWebInfResource("appengine-web.xml");
    }

    @Test
    public void testPull() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        TaskHandle th = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).param("foo", "bar").payload("foobar").etaMillis(15000));
        try {
            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
            Assert.assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            Assert.assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullWithTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        TaskHandle th = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo1").payload("foobar").etaMillis(15000));
        try {
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, "barfoo1");
            Assert.assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            Assert.assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullMultipleWithSameTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        TaskHandle th1 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo2").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo2").payload("foofoo").etaMillis(10000));
        try {
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, "barfoo2");
            Assert.assertEquals(2, handles.size());
            Assert.assertEquals(th2.getName(), handles.get(0).getName());
            Assert.assertEquals(th1.getName(), handles.get(1).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

    @Test
    public void testPullMultipleWithDiffTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        TaskHandle th1 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo3").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("qwerty").payload("foofoo").etaMillis(10000));
        TaskHandle th3 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo3").payload("foofoo").etaMillis(10000));
        try {
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, "barfoo3");
            Assert.assertEquals(2, handles.size());
            Assert.assertEquals(th3.getName(), handles.get(0).getName());
            Assert.assertEquals(th1.getName(), handles.get(1).getName());

            handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, "qwerty");
            Assert.assertEquals(1, handles.size());
            Assert.assertEquals(th2.getName(), handles.get(0).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }

    @Test
    public void testPullWithGroupTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        TaskHandle th1 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo3").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("qwerty").payload("foofoo").etaMillis(11000));
        TaskHandle th3 = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).tag("barfoo3").payload("foofoo").etaMillis(10000));
        try {
            LeaseOptions options = LeaseOptions.Builder.withLeasePeriod(1000L, TimeUnit.SECONDS).countLimit(100).groupByTag();
            List<TaskHandle> handles = queue.leaseTasks(options);
            Assert.assertEquals(2, handles.size());
            Assert.assertEquals(th3.getName(), handles.get(0).getName());
            Assert.assertEquals(th1.getName(), handles.get(1).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }
}
