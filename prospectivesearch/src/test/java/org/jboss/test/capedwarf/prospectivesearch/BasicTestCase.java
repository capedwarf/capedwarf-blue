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
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.api.prospectivesearch.Subscription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.capedwarf.prospectivesearch.CapedwarfProspectiveSearchService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BasicTestCase extends AbstractTestCase {

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(AbstractTestCase.class)
            .setWebXML(new StringAsset("<web/>"))
            .addAsWebInfResource("appengine-web.xml");
    }

    @Test
    public void testFactoryReturnsCapedwarfImplementation() {
        ProspectiveSearchService service = ProspectiveSearchServiceFactory.getProspectiveSearchService();
        assertEquals(CapedwarfProspectiveSearchService.class, service.getClass());
    }

    @Test
    public void testTopicIsCreatedWhenFirstSubscriptionForTopicIsCreated() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        assertTopicExists("myTopic");
    }

    @Test
    public void testTopicIsRemovedWhenLastSubscriptionForTopicIsDeleted() {
        service.subscribe("myTopic", "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        service.unsubscribe("myTopic", "mySubscription1");
        assertTopicExists("myTopic");
        service.unsubscribe("myTopic", "mySubscription2");
        assertTopicNotExists("myTopic");
    }

    @Test(expected = Exception.class)
    public void testSubscribeThrowsQuerySyntaxExceptionWhenSchemaIsEmpty() {
        service.subscribe("foo", "bar", 0, "title:hello", new HashMap<String, FieldType>());
    }

    @Test(expected = Exception.class)
    public void testUnsubscribeRemovesSubscription() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.unsubscribe("myTopic", "mySubscription");
        service.getSubscription("myTopic", "mySubscription");   // should throw IllegalArgumentException: No subscription with topic 'myTopic' and subId 'mySubscription'
    }

    @Test
    public void testSubscribeOverwritesPreviousSubscriptionWithSameId() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription", 0, "body:foo", createSchema("body", FieldType.STRING));

        assertEquals(1, service.listSubscriptions("myTopic").size());

        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        assertEquals("body:foo", subscription.getQuery());
    }

    @Test(expected = Exception.class)
    public void testUnsubscribeThrowsIllegalArgumentExceptionWhenTopicNotExists() {
        service.unsubscribe("myTopic", "mySubscription1");
    }

    @Test(expected = Exception.class)
    public void testUnsubscribeThrowsIllegalArgumentExceptionWhenSubIdNotExists() {
        service.subscribe("myTopic", "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.unsubscribe("myTopic", "nonExistentSubscription");
    }

    @Test
    public void testGetSubscription() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription("myTopic", "mySubscription");

        assertEquals("mySubscription", subscription.getId());
        assertEquals("title:hello", subscription.getQuery());
//        assertEquals(0, subscription.getExpirationTime());    // TODO
    }

    @Test(expected = Exception.class)
    public void testGetSubscriptionThrowsIllegalArgumentExceptionWhenNotExists() {
        service.getSubscription("myTopic", "nonExistentSubscription");
    }

    @Test
    public void testListSubscriptions() {
        service.subscribe("myTopic", "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        List<Subscription> subscriptions = service.listSubscriptions("myTopic");
        assertEquals(2, subscriptions.size());

        sortBySubId(subscriptions);

        Subscription subscription1 = subscriptions.get(0);
        assertEquals("mySubscription1", subscription1.getId());
        assertEquals("title:hello", subscription1.getQuery());

        Subscription subscription2 = subscriptions.get(1);
        assertEquals("mySubscription2", subscription2.getId());
        assertEquals("body:foo", subscription2.getQuery());
    }

    @Test
    public void testListTopicsReturnsInLexicographicalOrder() {
        service.subscribe("ccc", "subId", 0, "foo", createSchema("all", FieldType.STRING)); // TODO: what should the schema be like?
        service.subscribe("aaa", "subId", 0, "foo", createSchema("all", FieldType.STRING));
        service.subscribe("bbb", "subId", 0, "foo", createSchema("all", FieldType.STRING));

        List<String> topics = service.listTopics("", 1000);
        assertEquals(Arrays.asList("aaa", "bbb", "ccc"), topics);
    }

    private void assertTopicExists(String topic) {
        assertTrue("topic '" + topic + "' does not exist", getAllTopics().contains(topic));
    }

    private void assertTopicNotExists(String topic) {
        assertFalse("topic '" + topic + "' exists, but it shouldn't", getAllTopics().contains(topic));
    }

    private List<String> getAllTopics() {
        return service.listTopics("", 1000);
    }

}
