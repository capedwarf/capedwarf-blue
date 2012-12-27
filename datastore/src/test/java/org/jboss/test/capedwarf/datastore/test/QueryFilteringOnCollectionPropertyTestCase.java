package org.jboss.test.capedwarf.datastore.test;

import java.util.Arrays;

import com.google.appengine.api.datastore.Entity;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterPredicate;
import static org.junit.Assert.assertThat;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class QueryFilteringOnCollectionPropertyTestCase extends QueryTest {

    @Ignore("CAPEDWARF-69")
    @Test
    public void queryWithMultipleInequalityFiltersOnMultivaluedPropertyReturnsNothing() throws Exception {
        storeTestEntityWithSingleProperty(Arrays.asList(1, 2));
        assertThat(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, GREATER_THAN, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, LESS_THAN, 2))),
            queryReturnsNothing());
    }


    @Test
    public void queryWithMultipleEqualityFiltersOnMultivaluedPropertyReturnsEntityIfAllFiltersMatch() throws Exception {
        Entity entity = storeTestEntityWithSingleProperty(Arrays.asList(1, 2));
        assertThat(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, EQUAL, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, EQUAL, 2))),
            queryReturns(entity));
    }

    @Ignore("CAPEDWARF-69")
    @Test
    public void queryWithNotEqualFilter() throws Exception {
        Entity entity12 = storeTestEntityWithSingleProperty(Arrays.asList(1, 2));
        Entity entity123 = storeTestEntityWithSingleProperty(Arrays.asList(1, 2, 3));

        assertThat(whenFilteringWith(new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 1)), queryReturns(entity12));

        assertThat(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 2))),
            queryReturns(entity123));    // NOTE: should only match entity123, but not entity12
    }

}