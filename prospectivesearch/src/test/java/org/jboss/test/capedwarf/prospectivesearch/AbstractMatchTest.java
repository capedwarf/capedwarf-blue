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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractMatchTest extends AbstractTest {

    protected static final String SPECIAL_RESULT_RELATIVE_URI = "/_ah/prospective_search_special";
    protected static final String TOPIC = "myTopic";

    @Deployment
    public static WebArchive getDeployment() {
        final TestContext context = new TestContext();
        context.setWebXmlFile("web.xml");
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClasses(AbstractTest.class, AbstractMatchTest.class, MatchResponseServlet.class, SpecialMatchResponseServlet.class);
        return war;
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        MatchResponseServlet.clear();
        SpecialMatchResponseServlet.clear();
    }

    protected void assertServletWasInvoked() {
        if (!MatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }
    }

    protected Entity articleWithTitle(String title) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        return entity;
    }

    protected Entity articleWithTitleAndBody(String title, String body) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        entity.setProperty("body", body);
        return entity;
    }

    protected void assertServletWasInvokedWith(Entity entity) throws Exception {
        waitForJMSToKickIn();

        assertServletWasInvoked();

        Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }

        assertTrue("servlet was invoked with some other entity: " + lastReceivedDocument, entity.getProperties().equals(lastReceivedDocument.getProperties()));
    }

    protected void assertServletReceivedSubIds(String... subIds) throws Exception {
        waitForJMSToKickIn();

        assertServletWasInvoked();

        Set<String> expectedSubIds = new HashSet<String>(Arrays.asList(subIds));
        Set<String> receivedSubIds = new HashSet<String>(MatchResponseServlet.getAllSubIds());
        assertEquals("servlet was invoked with wrong subIds", expectedSubIds, receivedSubIds);
    }

    protected void assertSpecialServletWasInvokedWith(Entity entity) throws Exception {
        waitForJMSToKickIn();

        if (!SpecialMatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }

        Entity lastReceivedDocument = SpecialMatchResponseServlet.getLastReceivedDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }
    }

    protected void assertServletWasNotInvoked() throws Exception {
        waitForJMSToKickIn();

        if (MatchResponseServlet.isInvoked()) {
            Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
            fail("servlet was invoked with: " + lastReceivedDocument);
        }
    }

    protected void waitForJMSToKickIn() throws InterruptedException {
        Thread.sleep(3000);
    }

}
