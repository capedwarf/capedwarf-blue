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

package org.jboss.test.capedwarf.prospectivesearch.test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.QuerySyntaxException;
import com.google.appengine.api.prospectivesearch.Subscription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class BasicTestCase extends AbstractTest {

    @Deployment
    public static WebArchive getDeployment() {
        return getCapedwarfDeployment().addClass(AbstractTest.class);
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

    @Test
    public void testSubscribeThrowsQuerySyntaxExceptionWhenSchemaIsEmpty() {
        if (runningInsideDevAppEngine() && isJBossImpl(service) == false) {
            // we shouldn't test this on dev appserver, since it doesn't throw this exception
            return;
        }
        try {
            service.subscribe("foo", "bar", 0, "title:hello", new HashMap<String, FieldType>());
            fail("Expected QuerySyntaxException: Schema is empty");
        } catch (QuerySyntaxException e) {
            // pass
        }
    }

    @Test
    public void testSubscriptionIsAutomaticallyRemovedAfterLeaseDurationSeconds() throws Exception {
        service.subscribe("foo", "bar", 5, "title:hello", createSchema("title", FieldType.STRING));
        assertSubscriptionExists("foo", "bar");
        sleepSeconds(10);
        assertSubscriptionNotExists("foo", "bar");
    }

    @Test
    public void testUnsubscribeRemovesSubscription() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.unsubscribe("myTopic", "mySubscription");
        assertSubscriptionNotExists("myTopic", "mySubscription");
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
    }

    @Test
    public void testSubscriptionWithoutLeaseTimeSecondsPracticallyNeverExpires() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        long expirationTime = subscription.getExpirationTime();

        if (runningInsideDevAppEngine() && isJBossImpl(service) == false) {
            assertEquals(0L, expirationTime);
        } else {
            long expected = todayPlusHundredYears().getTime() / 1000;
            assertTrue("subscription should not expire at least 100 years", expirationTime > expected);
        }
    }

    @Test
    public void testSubscriptionWithLeaseTimeSecondsHasCorrectExpirationTime() {
        service.subscribe("myTopic", "mySubscription", 500, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        assertEquals(System.currentTimeMillis() / 1000 + 500, subscription.getExpirationTime(), 10.0);
    }

    private Date todayPlusHundredYears() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 100);
        return cal.getTime();
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

    private void assertSubscriptionExists(String topic, String subId) {
        try {
            service.getSubscription(topic, subId);
        } catch (IllegalArgumentException e) {
            fail("subscription with topic " + topic + " and subId " + subId + " does not exists, but it should");
        }
    }

    private void assertSubscriptionNotExists(String topic, String subId) {
        try {
            service.getSubscription(topic, subId);
            fail("subscription with topic " + topic + " and subId " + subId + " exists, but it shouldn't");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    private List<String> getAllTopics() {
        return service.listTopics("", 1000);
    }

    private void sleepSeconds(int seconds) throws Exception {
        sync(1000L * seconds);
    }

}
