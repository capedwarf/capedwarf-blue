package org.jboss.test.capedwarf.cluster;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.api.prospectivesearch.QuerySyntaxException;
import com.google.appengine.api.prospectivesearch.Subscription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.cluster.ProspectiveSearchMatchResponseServlet.InvocationData;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class ProspectiveSearchTestCase extends BaseTest {

    private static final String TOPIC = "myTopic";

    protected ProspectiveSearchService service;

    @Before
    public void setUp() {
        service = ProspectiveSearchServiceFactory.getProspectiveSearchService();
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void cleanUpOnDep1() {
        clear();
        assertEquals(0, getAllTopics().size());
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void cleanUpOnDep2() {
        clear();
        assertEquals(0, getAllTopics().size());
    }

    @InSequence(100)
    @Test
    @OperateOnDeployment("dep1")
    public void testTopicIsCreatedWhenFirstSubscriptionForTopicIsCreatedOnDep1() {
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        waitForSync();
        assertTopicExists("myTopic");
    }

    @InSequence(110)
    @Test
    @OperateOnDeployment("dep2")
    public void testTopicIsCreatedWhenFirstSubscriptionForTopicIsCreatedOnDep2() {
        assertTopicExists("myTopic");
    }

    @InSequence(120)
    @Test
    @OperateOnDeployment("dep1")
    public void testTopicIsRemovedWhenLastSubscriptionForTopicIsDeletedOnDep1() {
        clear();
        service.subscribe("myTopic", "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        service.unsubscribe("myTopic", "mySubscription1");
        waitForSync();
        assertTopicExists("myTopic");
        service.unsubscribe("myTopic", "mySubscription2");
        waitForSync();
        assertTopicNotExists("myTopic");
    }

    @InSequence(130)
    @Test
    @OperateOnDeployment("dep2")
    public void testTopicIsRemovedWhenLastSubscriptionForTopicIsDeletedOnDep2() {
        assertTopicNotExists("myTopic");
    }

    @InSequence(140)
    @Test
    @OperateOnDeployment("dep1")
    public void testSubscribeThrowsQuerySyntaxExceptionWhenSchemaIsEmptyOnDep1() {
        // we shouldn't test this on dev appserver, since it doesn't throw this exception
        try {
            service.subscribe("foo", "bar", 0, "title:hello", new HashMap<String, FieldType>());
            fail("Expected QuerySyntaxException: Schema is empty");
        } catch (QuerySyntaxException e) {
            // pass
        }
    }

    @InSequence(150)
    @Test
    @OperateOnDeployment("dep2")
    public void testSubscribeThrowsQuerySyntaxExceptionWhenSchemaIsEmptyOnDep2() {
        // we shouldn't test this on dev appserver, since it doesn't throw this exception
        try {
            service.subscribe("foo", "bar", 0, "title:hello", new HashMap<String, FieldType>());
            fail("Expected QuerySyntaxException: Schema is empty");
        } catch (QuerySyntaxException e) {
            // pass
        }
    }

    @InSequence(160)
    @Test
    @OperateOnDeployment("dep1")
    public void testSubscriptionIsAutomaticallyRemovedAfterLeaseDurationSecondsOnDep1() throws Exception {
        clear();
        service.subscribe("foo", "bar", 8, "title:hello", createSchema("title", FieldType.STRING));
        waitForSync();
        assertSubscriptionExists("foo", "bar");
    }

    @InSequence(161)
    @Test
    @OperateOnDeployment("dep2")
    public void testSubscriptionIsAutomaticallyRemovedAfterLeaseDurationSecondsOnDep2() throws Exception {
        assertSubscriptionExists("foo", "bar");
        sync(8000);
        assertSubscriptionNotExists("foo", "bar");
    }

    @InSequence(162)
    @Test
    @OperateOnDeployment("dep1")
    public void testSubscriptionIsAutomaticallyRemovedAfterLeaseDurationSecondsOnDep1_isRemoved() throws Exception {
        assertSubscriptionNotExists("foo", "bar");
    }

    @InSequence(170)
    @Test
    @OperateOnDeployment("dep1")
    public void testSubscribeOverwritesPreviousSubscriptionWithSameIdOnDep1() {
        clear();
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription", 0, "body:foo", createSchema("body", FieldType.STRING));
        waitForSync();
        assertEquals(1, service.listSubscriptions("myTopic").size());

        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        assertEquals("mySubscription", subscription.getId());
        assertEquals("body:foo", subscription.getQuery());
    }

    @InSequence(180)
    @Test
    @OperateOnDeployment("dep2")
    public void testSubscribeOverwritesPreviousSubscriptionWithSameIdOnDep2() {
        assertEquals(1, service.listSubscriptions("myTopic").size());

        service.subscribe("myTopic", "mySubscription", 0, "body:foo", createSchema("body", FieldType.STRING));
        waitForSync();
        assertEquals(1, service.listSubscriptions("myTopic").size());

        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        assertEquals("mySubscription", subscription.getId());
        assertEquals("body:foo", subscription.getQuery());
    }

    @InSequence(190)
    @Test
    @OperateOnDeployment("dep1")
    public void testSubscriptionWithoutLeaseTimeSecondsPracticallyNeverExpiresOnDep1() {
        clear();
        service.subscribe("myTopic", "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        waitForSync();
        long expirationTime = subscription.getExpirationTime();

        long expected = todayPlusHundredYears().getTime() / 1000;
        assertTrue("subscription should not expire at least 100 years", expirationTime > expected);
    }

    @InSequence(200)
    @Test
    @OperateOnDeployment("dep2")
    public void testSubscriptionWithoutLeaseTimeSecondsPracticallyNeverExpiresOnDep2() {
        Subscription subscription = service.getSubscription("myTopic", "mySubscription");
        long expirationTime = subscription.getExpirationTime();

        long expected = todayPlusHundredYears().getTime() / 1000;
        assertTrue("subscription should not expire at least 100 years", expirationTime > expected);
    }

    @InSequence(210)
    @Test
    @OperateOnDeployment("dep1")
    public void testListSubscriptionsOnDep1() {
        clear();
        service.subscribe("myTopic", "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("myTopic", "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        waitForSync();
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

    @InSequence(220)
    @Test
    @OperateOnDeployment("dep2")
    public void testListSubscriptionsOnDep2() {
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

    @InSequence(230)
    @Test
    @OperateOnDeployment("dep1")
    public void testListTopicsReturnsInLexicographicalOrderOnDep1() {
        clear();
        service.subscribe("ccc", "subId", 0, "foo", createSchema("all", FieldType.STRING)); // TODO: what should the schema be like?
        service.subscribe("aaa", "subId", 0, "foo", createSchema("all", FieldType.STRING));
        service.subscribe("bbb", "subId", 0, "foo", createSchema("all", FieldType.STRING));

        waitForSync();
        List<String> topics = service.listTopics("", 1000);
        assertEquals(Arrays.asList("aaa", "bbb", "ccc"), topics);
    }

    @InSequence(240)
    @Test
    @OperateOnDeployment("dep2")
    public void testListTopicsReturnsInLexicographicalOrderOnDep2() {
        List<String> topics = service.listTopics("", 1000);
        assertEquals(Arrays.asList("aaa", "bbb", "ccc"), topics);
    }

    @InSequence(300)
    @Test
    @OperateOnDeployment("dep1")
    public void testMatchInvokesServletWhenSearchMatchesDep1() throws Exception {
        clear();
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
    }

    @InSequence(310)
    @Test
    @OperateOnDeployment("dep2")
    public void testMatchInvokesServletWhenSearchMatchesDep2() throws Exception {
        waitForSync();
        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC);
        waitForSync();
        assertServletWasInvokedWith(entity);
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void removeAllSubscriptionsOnDep1() {
        removeAllSubscriptions();
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep2")
    public void removeAllSubscriptionsOnDep2() {
        removeAllSubscriptions();
    }

    private void removeAllSubscriptions() {
        List<String> topics = service.listTopics("", 1000);
        for (String topic : topics) {
            List<Subscription> subscriptions = service.listSubscriptions(topic);
            for (Subscription subscription : subscriptions) {
                service.unsubscribe(topic, subscription.getId());
            }
        }
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

    protected Map<String, FieldType> createSchema(String field, FieldType type) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field, type);
        return schema;
    }

    protected Map<String, FieldType> createSchema(String field1, FieldType type1, String field2, FieldType type2) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field1, type1);
        schema.put(field2, type2);
        return schema;
    }

    private Date todayPlusHundredYears() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 100);
        return cal.getTime();
    }

    protected void sortBySubId(List<Subscription> subscriptions) {
        Collections.sort(subscriptions, new Comparator<Subscription>() {
            public int compare(Subscription o1, Subscription o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private Entity articleWithTitle(String title) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        return entity;
    }

    private void assertServletWasInvokedWith(Entity entity) throws Exception {
        DatastoreService dStoreService = DatastoreServiceFactory.getDatastoreService();
        InvocationData invocationData = ProspectiveSearchMatchResponseServlet.getInvocationData(dStoreService);

        if (invocationData == null) {
            fail("servlet was not invoked");
        }

        Entity lastReceivedDocument = invocationData.getDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }

        assertTrue("servlet was invoked with some other entity: " + lastReceivedDocument, entity.getProperties().equals(lastReceivedDocument.getProperties()));
    }

    private void waitForSync() {
        sync();
    }

    private void clear() {
        removeAllSubscriptions();
    }

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        final TestContext context = new TestContext().setWebXmlFile("web.xml");
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClass(ProspectiveSearchMatchResponseServlet.class);
        return war;
    }
}
