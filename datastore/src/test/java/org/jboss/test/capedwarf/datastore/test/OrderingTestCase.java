package org.jboss.test.capedwarf.datastore.test;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

/**
 *
 */
//@Ignore
@RunWith(Arquillian.class)
@org.junit.experimental.categories.Category(All.class)
public class OrderingTestCase extends QueryTest {


    @Test
    public void testFiltering() {
        List<List<?>> values = asList(
            singletonList(null),
            asList(10, new Rating(10)),
            asList(20, new Rating(20)),
            singletonList(createDate(2013, 1, 1)),
            singletonList(createDate(2013, 5, 5)),
            singletonList(false),
            singletonList(true),
            asList(
                "sip sip",
                new ShortBlob("sip sip".getBytes()),
                new PostalAddress("sip sip"),
                new PhoneNumber("sip sip"),
                new Email("sip sip"),
                new IMHandle(IMHandle.Scheme.sip, "sip"),   // this is stored as "sip sip"
                new Link("sip sip"),
                new Category("sip sip"),
                new BlobKey("sip sip")
            ),
            asList(
                "xmpp xmpp",
                new ShortBlob("xmpp xmpp".getBytes()),
                new PostalAddress("xmpp xmpp"),
                new PhoneNumber("xmpp xmpp"),
                new Email("xmpp xmpp"),
                new IMHandle(IMHandle.Scheme.xmpp, "xmpp"), // this is stored as "xmpp xmpp"
                new Link("xmpp xmpp"),
                new Category("xmpp xmpp"),
                new BlobKey("xmpp xmpp")
            ),
            singletonList(10.0),
            singletonList(20.0),
            singletonList(new GeoPt(10f, 10f)),
            singletonList(new GeoPt(20f, 20f)),
            singletonList(new User("aaa", "aaa")),
            singletonList(new User("bbb", "bbb")),
            singletonList(KeyFactory.createKey("kind", "aaa")),
            singletonList(KeyFactory.createKey("kind", "bbb"))
        );


        List<List<Entity>> entities = new ArrayList<List<Entity>>();
        for (List<?> values2 : values) {
            ArrayList<Entity> entities2 = new ArrayList<Entity>();
            entities.add(entities2);
            for (Object value : values2) {
                entities2.add(storeTestEntityWithSingleProperty(value));
            }
        }

        for (int i = 0; i < values.size(); i++) {
            List<?> values2 = values.get(i);
            for (Object value : values2) {
                assertThat("when filtering with = " + value, whenFilteringBy(EQUAL, value), queryReturns(entities.get(i).toArray(new Entity[0])));
                assertThat("when filtering with <= " + value, whenFilteringBy(LESS_THAN_OR_EQUAL, value), queryReturns(flatten(entities.subList(0, i + 1))));
                assertThat("when filtering with < " + value, whenFilteringBy(LESS_THAN, value), queryReturns(flatten(entities.subList(0, i))));
                assertThat("when filtering with >= " + value, whenFilteringBy(GREATER_THAN_OR_EQUAL, value), queryReturns(flatten(entities.subList(i, entities.size()))));
                assertThat("when filtering with > " + value, whenFilteringBy(GREATER_THAN, value), queryReturns(flatten(entities.subList(i+1, entities.size()))));
            }
        }

        for (List<Entity> entities2 : entities) {
            for (Entity entity : entities2) {
                service.delete(entity.getKey());
            }
        }
    }

    private Entity[] flatten(List<List<Entity>> entities) {
        List<Entity> list = new ArrayList<Entity>();
        for (List<Entity> entities2 : entities) {
            list.addAll(entities2);
        }
        return list.toArray(new Entity[list.size()]);
    }

}
