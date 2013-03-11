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

package org.jboss.test.capedwarf.tasks.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.capedwarf.common.io.IOUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.tasks.support.DefaultQueueServlet;
import org.jboss.test.capedwarf.tasks.support.PrintServlet;
import org.jboss.test.capedwarf.tasks.support.RequestData;
import org.jboss.test.capedwarf.tasks.support.RetryTestServlet;
import org.jboss.test.capedwarf.tasks.support.TestQueueServlet;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class TasksTestBase extends TestBase {
    @Deployment
    public static Archive getDeployment() {
        final TestContext context = TestContext.asDefault();
        context.setWebXmlFile("web-q.xml");
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClass(TasksTestBase.class);
        war.addClass(IOUtils.class);
        war.addClass(RequestData.class);
        war.addClass(DefaultQueueServlet.class);
        war.addClass(TestQueueServlet.class);
        war.addClass(PrintServlet.class);
        war.addClass(RetryTestServlet.class);
        war.addAsWebInfResource("queue.xml");
        return war;
    }
}
