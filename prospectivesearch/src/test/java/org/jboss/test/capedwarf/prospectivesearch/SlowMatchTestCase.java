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

package org.jboss.test.capedwarf.prospectivesearch;

import com.google.appengine.api.prospectivesearch.FieldType;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_RELATIVE_URL;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_TASK_QUEUE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SlowMatchTestCase extends AbstractMatchTest {

    @Test
    public void testMatchHonorsResultBatchSize() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo2", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo3", 0, "title:foo", createSchema("title", FieldType.STRING));

        int resultBatchSize = 2;
        service.match(articleWithTitle("Foo foo"), TOPIC, "", DEFAULT_RESULT_RELATIVE_URL, DEFAULT_RESULT_TASK_QUEUE_NAME, resultBatchSize, true);

        assertServletReceivedSubIds("foo1", "foo2", "foo3");

        int expectedInvocationCount = 2; // Math.ceil(3 / 2) = 2
        assertEquals("incorrect servlet invocation count", expectedInvocationCount, MatchResponseServlet.getInvocationCount());

        for (MatchResponseServlet.InvocationData invocationData : MatchResponseServlet.getInvocations()) {
            assertTrue("batch was too large", invocationData.getSubIds().length <= resultBatchSize);
        }
    }

    protected void waitForJMSToKickIn() throws InterruptedException {
        Thread.sleep(10000);
    }

}
