/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.testsuite.common.support;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;

/**
 * This class starts pull workers for processing tasks from pull queues
 *
 * @author Bart Vanbrabant
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PullWorkerRegistration implements ServletContextListener {
    private volatile boolean cycle = true;

    public void contextInitialized(ServletContextEvent event) {
        Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
            public void run() {
                try {
                    Queue q = QueueFactory.getQueue("pull-queue");
                    System.out.println("Started worker");
                    while (cycle) {
                        List<TaskHandle> tasks = q.leaseTasks(10, TimeUnit.SECONDS, 100);

                        for (TaskHandle th : tasks) {
                            try {
                                List<Map.Entry<String, String>> params = th.extractParams();

                                System.out.println(params);
                                System.out.println("Did something.");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (UnsupportedOperationException e) {
                                e.printStackTrace();
                            }

                            q.deleteTask(th);
                        }

                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted in loop:", ex);
                }
            }
        });
        thread.start();
    }

    /**
     * Stop all worker threads when the servlet is destroyed.
     */
    public void contextDestroyed(ServletContextEvent event) {
        cycle = false;
    }
}