package org.jboss.test.capedwarf.datastore.test;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static org.junit.Assert.assertThat;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryFilteringByStringPropertyTypeTestCase extends QueryTestCase {

    @Ignore("Need hibernate-search-4.0.0.CR2 for this to work")
    @Test
    public void queryByEqualReturnsEntityWithEqualPropertyValue() throws Exception {
        testEqualityQueries("foo", "bar");
        testInequalityQueries("aaa", "bbb", "ccc");
    }

    @Test
    public void queryDoesNotReturnResultIfFilterIsSubstringOfProperty() throws Exception {
        storeTestEntityWithSingleProperty("John Doe");
        Query query = createQuery(EQUAL, "John");
        assertNoResults(query);
    }

    @Test
    public void testQueryByIn() throws Exception {
        Entity john = storeTestEntityWithSingleProperty("John");
        Entity kate = storeTestEntityWithSingleProperty("Kate");
        Entity ashley = storeTestEntityWithSingleProperty("Ashley");

        assertThat(whenFilteringBy(IN, Arrays.asList("Kate", "Ashley")), queryReturns(kate, ashley));
    }

}
