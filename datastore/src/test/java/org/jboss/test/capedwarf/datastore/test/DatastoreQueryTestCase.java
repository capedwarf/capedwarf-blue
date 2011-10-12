package org.jboss.test.capedwarf.datastore.test;

import com.google.appengine.api.datastore.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class DatastoreQueryTestCase {

    protected DatastoreService service;

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource("jboss/jboss-deployment-structure.xml", "jboss-deployment-structure.xml");
    }

    @Before
    public void setUp() {
        service = DatastoreServiceFactory.getDatastoreService();
    }

    @Ignore
    @Test
    public void queryingByKindWorks() throws Exception {
        Entity person = new Entity("Person");
        service.put(person);

        PreparedQuery preparedQuery = service.prepare(new Query("Person"));
        Entity personFromQuery = preparedQuery.asSingleEntity();

        Assert.assertEquals(person, personFromQuery);
    }

    @Ignore
    @Test
    public void queryingByStringProperty() throws Exception {

        Entity person = new Entity(KeyFactory.createKey("Person", 1));
        person.setProperty("name", "John");
        person.setProperty("lastName", "Doe");
        service.put(person);

        Query query = new Query("Person");
        query.addFilter("name", Query.FilterOperator.EQUAL, "John");

        PreparedQuery preparedQuery = service.prepare(query);

        Entity personFromQuery = preparedQuery.asSingleEntity();

        Assert.assertEquals(person, personFromQuery);
    }

    @Ignore
    @Test
    public void queryingByEqualsWorks() throws Exception {
        Entity johnDoe = new Entity("Person");
        johnDoe.setProperty("firstName", "John");
        johnDoe.setProperty("lastName", "Doe");
        service.put(johnDoe);

        Query query = new Query("Person");
        query.addFilter("lastName", Query.FilterOperator.EQUAL, "Doe");
        PreparedQuery preparedQuery = service.prepare(query);
        Entity johnDoeFromQuery = preparedQuery.asSingleEntity();

        Assert.assertEquals(johnDoe, johnDoeFromQuery);
    }


}
