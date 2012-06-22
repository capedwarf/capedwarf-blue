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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MatchTestCase extends AbstractTestCase {

    private static final String SPECIAL_RESULT_RELATIVE_URI = "/_ah/prospective_search_special";
    private static final String TOPIC = "myTopic";

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(AbstractTestCase.class, MatchResponseServlet.class, SpecialMatchResponseServlet.class)
            .addAsWebInfResource("web.xml")
            .addAsWebInfResource("appengine-web.xml");
    }

    @After
    public void tearDown() throws Exception {
        MatchResponseServlet.clear();
        SpecialMatchResponseServlet.clear();
    }

    @Test
    public void testMatchInvokesServletWhenSearchMatches() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchDoesNotInvokeServletWhenSearchDoesNotMatch() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:foo", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Bar");
        service.match(entity, TOPIC);

        assertServletWasNotInvoked();
    }

    private Entity articleWithTitle(String title) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        return entity;
    }

    @Test
    public void testMatchHonorsResultRelativeUri() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC, "", SPECIAL_RESULT_RELATIVE_URI, ProspectiveSearchService.DEFAULT_RESULT_TASK_QUEUE_NAME, ProspectiveSearchService.DEFAULT_RESULT_BATCH_SIZE, true);

        assertSpecialServletWasInvokedWith(entity);
    }

    private void assertServletWasInvokedWith(Entity entity) throws Exception {
        waitForJMSToKickIn();

        if (!MatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }

        Entity lastReceivedDocument = MatchResponseServlet.getLastReceivedDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }

        assertTrue("servlet was invoked with some other entity: " + lastReceivedDocument, entity.getProperties().equals(lastReceivedDocument.getProperties()));
    }

    private void assertSpecialServletWasInvokedWith(Entity entity) throws Exception {
        waitForJMSToKickIn();

        if (!SpecialMatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }

        Entity lastReceivedDocument = SpecialMatchResponseServlet.getLastReceivedDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }
    }

    private void assertServletWasNotInvoked() throws Exception {
        waitForJMSToKickIn();

        if (MatchResponseServlet.isInvoked()) {
            Entity lastReceivedDocument = MatchResponseServlet.getLastReceivedDocument();
            fail("servlet was invoked with: " + lastReceivedDocument);
        }
    }

    private void waitForJMSToKickIn() throws InterruptedException {
        Thread.sleep(3000);
    }

}
