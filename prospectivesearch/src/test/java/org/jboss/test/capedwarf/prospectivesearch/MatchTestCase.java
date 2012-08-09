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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.prospectivesearch.FieldType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_BATCH_SIZE;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_RELATIVE_URL;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_TASK_QUEUE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class MatchTestCase extends AbstractTest {

    private static final String SPECIAL_RESULT_RELATIVE_URI = "/_ah/prospective_search_special";
    private static final String TOPIC = "myTopic";

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(AbstractTest.class, MatchResponseServlet.class, SpecialMatchResponseServlet.class)
            .addAsWebInfResource("web.xml")
            .addAsWebInfResource("appengine-web.xml");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
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

    @Test
    public void testMatchUsesGoogleQuerySyntax() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:\"Hello World\" body:article", createSchema("title", FieldType.STRING, "body", FieldType.STRING));

        Entity entity = articleWithTitleAndBody("Hello World", "This is the body of the article.");
        service.match(entity, TOPIC);
        assertServletWasInvokedWith(entity);

        MatchResponseServlet.clear();

        entity = articleWithTitleAndBody("Hello World", "This body does not contain the word matched by foo subscription.");
        service.match(entity, TOPIC);
        assertServletWasNotInvoked();
    }

    @Test
    public void testMatchOnlyMatchesDocumentsInSameTopic() throws Exception {
        service.subscribe("topic1", "foo", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("topic2", "bar", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, "topic1");

        assertServletReceivedSubIds("foo");
    }

    @Test
    public void testMatchHonorsResultRelativeUri() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC, "", SPECIAL_RESULT_RELATIVE_URI, DEFAULT_RESULT_TASK_QUEUE_NAME, DEFAULT_RESULT_BATCH_SIZE, true);

        assertSpecialServletWasInvokedWith(entity);
    }

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

    @Test
    public void testServletReceivesCorrectSubscriptionIds() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo2", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "bar", 0, "title:bar", createSchema("title", FieldType.STRING));
        service.match(articleWithTitle("Foo foo"), TOPIC);

        assertServletReceivedSubIds("foo1", "foo2");
    }

    @Test
    public void testServletReceivesResultKeyParameter() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));

        String expectedKey = "myResultKey";
        service.match(articleWithTitle("Foo foo"), TOPIC, expectedKey);

        waitForJMSToKickIn();
        assertServletWasInvoked();

        String receivedKey = MatchResponseServlet.getLastInvocationData().getKey();
        assertEquals("servlet was invoked with wrong key", expectedKey, receivedKey);
    }

    @Test
    public void testServletReceivesTopicParameter() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.match(articleWithTitle("Foo foo"), TOPIC);

        waitForJMSToKickIn();
        assertServletWasInvoked();

        String receivedTopic = MatchResponseServlet.getLastInvocationData().getTopic();
        assertEquals("servlet was invoked with wrong topic", TOPIC, receivedTopic);
    }

    @Test
    public void testServletReceivesDocumentOnlyWhenFlagIsTrue() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));

        boolean resultReturnDocument = false;
        service.match(articleWithTitle("Foo foo"), TOPIC, "", DEFAULT_RESULT_RELATIVE_URL, DEFAULT_RESULT_TASK_QUEUE_NAME, DEFAULT_RESULT_BATCH_SIZE, resultReturnDocument);

        waitForJMSToKickIn();
        assertServletWasInvoked();
        assertNull("servlet should not have received document", MatchResponseServlet.getLastInvocationData().getDocument());
    }

    @Test
    public void testMatchOnStringField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:happy", createSchema("title", FieldType.STRING));

        Entity entity = new Entity("article");
        entity.setProperty("title", "happy feet");
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnStringListField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:sad", createSchema("title", FieldType.STRING));

        Entity entity = new Entity("article");
        entity.setProperty("title", Arrays.asList("happy feet", "sad head"));
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnTextField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:happy", createSchema("title", FieldType.TEXT));

        Entity entity = new Entity("article");
        entity.setProperty("title", new Text("happy feet"));
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Ignore("TODO - use proper analyzer")
    @Test
    public void testMatchOnIntegerField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "length=500", createSchema("length", FieldType.INT32));

        Entity entity = new Entity("article");
        entity.setProperty("length", 500);
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    private void assertServletWasInvoked() {
        if (!MatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }
    }

    private Entity articleWithTitle(String title) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        return entity;
    }

    private Entity articleWithTitleAndBody(String title, String body) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        entity.setProperty("body", body);
        return entity;
    }

    private void assertServletWasInvokedWith(Entity entity) throws Exception {
        waitForJMSToKickIn();

        assertServletWasInvoked();

        Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }

        assertTrue("servlet was invoked with some other entity: " + lastReceivedDocument, entity.getProperties().equals(lastReceivedDocument.getProperties()));
    }

    private void assertServletReceivedSubIds(String... subIds) throws Exception {
        waitForJMSToKickIn();

        assertServletWasInvoked();

        Set<String> expectedSubIds = new HashSet<String>(Arrays.asList(subIds));
        Set<String> receivedSubIds = new HashSet<String>(MatchResponseServlet.getAllSubIds());
        assertEquals("servlet was invoked with wrong subIds", expectedSubIds, receivedSubIds);
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
            Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
            fail("servlet was invoked with: " + lastReceivedDocument);
        }
    }

    private void waitForJMSToKickIn() throws InterruptedException {
        Thread.sleep(3000);
    }

}
