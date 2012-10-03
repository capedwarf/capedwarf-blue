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

package org.jboss.test.capedwarf.search;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.search.AddResponse;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class SearchTestCase extends AbstractTest {


    @Test
    public void testSearchBySingleField() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.add(newDocument("foobbb", newField("foo").setText("bbb")));

        assertSearchYields(index, "foo:aaa", "fooaaa");
    }

    @Test
    public void testSearchByTwoFields() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.add(newDocument("foobbb", newField("foo").setText("aaa"), newField("bar").setText("ccc")));

        assertSearchYields(index, "foo:aaa bar:bbb", "fooaaa");
    }

    @Test
    public void testSearchDisjunction() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.add(newDocument("foobbb", newField("foo").setText("bbb"), newField("bar").setText("ccc")));
        index.add(newDocument("fooccc", newField("foo").setText("ccc"), newField("bar").setText("bbb")));

        assertSearchYields(index, "foo:aaa OR bar:bbb", "fooaaa", "fooccc");
    }

    @Test
    public void testSearchByTerm() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("bar aaa baz")));
        index.add(newDocument("foobbb", newField("foo").setText("bar bbb baz")));
        index.add(newDocument("fooaaa2", newField("foo").setText("baraaabaz")));

        assertSearchYields(index, "foo:aaa", "fooaaa");
    }

    @Test
    public void testSearchByPhrase() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa bbb ccc ddd")));
        index.add(newDocument("foobbb", newField("foo").setText("bbb ccc aaa ddd")));

        assertSearchYields(index, "foo:\"aaa bbb ccc\"", "fooaaa");
    }

    @Test
    public void testSearchByStringEquality() {
        Index index = getTestIndex();
        index.add(newDocument("d", newField("foo").setText("ddd")));
        index.add(newDocument("b", newField("foo").setText("bbb")));
        index.add(newDocument("c", newField("foo").setText("ccc")));
        index.add(newDocument("a", newField("foo").setText("aaa")));

        assertSearchYields(index, "foo = bbb", "b");
    }

    @Test
    public void testSearchByDateEqualityAndInequality() {
        if (runningInsideDevAppEngine()) {
            // this test only works on dev server if the _system_ timezone is set to UTC
            return;
        }

        Index index = getTestIndex();
        index.add(newDocument("d", newField("date").setDate(createDate(2004, 5, 5))));
        index.add(newDocument("b", newField("date").setDate(createDate(2002, 5, 5))));
        index.add(newDocument("c", newField("date").setDate(createDate(2003, 5, 5))));
        index.add(newDocument("a", newField("date").setDate(createDate(2001, 5, 5))));

        assertSearchYields(index, "date > 2002-05-05", "c", "d");
        assertSearchYields(index, "date >= 2002-05-05", "b", "c", "d");
        assertSearchYields(index, "date < 2002-05-05", "a");
        assertSearchYields(index, "date <= 2002-05-05", "a", "b");
        assertSearchYields(index, "date = 2002-05-05", "b");
    }

    @Test
    public void testSearchByNumberEqualityAndInequality() {
        Index index = getTestIndex();
        index.add(newDocument("d", newField("num").setNumber(4.0d)));
        index.add(newDocument("b", newField("num").setNumber(2.0d)));
        index.add(newDocument("c", newField("num").setNumber(3.0d)));
        index.add(newDocument("a", newField("num").setNumber(1.0d)));

        assertSearchYields(index, "num > 2", "c", "d");
        assertSearchYields(index, "num >= 2", "b", "c", "d");
        assertSearchYields(index, "num < 2", "a");
        assertSearchYields(index, "num <= 2", "a", "b");
        assertSearchYields(index, "num = 2", "b");
    }

    @Test
    public void testSearchOnHtmlFieldIgnoresTags() {
        Index index = getTestIndex();
        index.add(newDocument("a", newField("html").setHTML("<html><body>hello</body></html>")));
        index.add(newDocument("b", newField("html").setHTML("<html><body>body</body></html>")));

        assertSearchYields(index, "html:body", "b");
    }

    @Test
    public void testSearchForNumberInText() {
        Index index = getTestIndex();
        index.add(newDocument("a", newField("text").setText("Founded in 1993, Red Hat has its corporate headquarters in Raleigh, North Carolina with satellite offices worldwide.")));
        assertSearchYields(index, "text:1993", "a");
    }

    @Test
    public void testSearchForLocationWithinSpecifiedDistance() {
        if (runningInsideDevAppEngine()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.add(newDocument("a", newField("location").setGeoPoint(new GeoPoint(45.0, 15.0))));
        index.add(newDocument("b", newField("location").setGeoPoint(new GeoPoint(60.0, 40.0))));
        assertSearchYields(index, "distance(location, geopoint(45.0, 15.0)) < 1000", "a");
    }

    @Test
    public void testArgumentsOfDistanceFunctionCanBeSwapped() {
        if (runningInsideDevAppEngine()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.add(newDocument("a", newField("location").setGeoPoint(new GeoPoint(45.0, 15.0))));
        index.add(newDocument("b", newField("location").setGeoPoint(new GeoPoint(60.0, 40.0))));
        assertSearchYields(index, "distance(geopoint(45.0, 15.0), location) < 1000", "a");
    }

    @Test
    public void testSearchForLocationYieldsResultsInsideRadiusButNotInsideSquare() {
        if (runningInsideDevAppEngine()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.add(newDocument("a", newField("location").setGeoPoint(new GeoPoint(46.051464, 14.515833))));
        index.add(newDocument("b", newField("location").setGeoPoint(new GeoPoint(46.046111, 14.513889))));
        assertSearchYields(index, "distance(location, geopoint(46.051464, 14.506097)) < 800", "a");
    }

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.clear(Calendar.MILLISECOND);
        return cal.getTime();
    }

    private void assertSearchYields(Index index, String queryString, String... documentIds) {
        Collection<ScoredDocument> scoredDocuments = getScoredDocuments(index, queryString, documentIds);

        Set<String> expectedDocumentIds = new HashSet<String>(Arrays.asList(documentIds));
        for (ScoredDocument scoredDocument : scoredDocuments) {
            boolean wasContained = expectedDocumentIds.remove(scoredDocument.getId());
            if (!wasContained) {
                fail("Search \"" + queryString + "\" yielded unexpected document id: " + scoredDocument.getId());
            }
        }
    }

    private Collection<ScoredDocument> getScoredDocuments(Index index, String queryString, String[] documentIds) {
        Results<ScoredDocument> results = index.search(queryString);
        Collection<ScoredDocument> scoredDocuments = results.getResults();
        System.out.println("-------------------------------");
        System.out.println("queryString = " + queryString);
        System.out.println("scoredDocuments = " + scoredDocuments);

        for (ScoredDocument scoredDocument : scoredDocuments) {
            System.out.println("scoredDocument = " + scoredDocument);
        }
        assertNumberOfResults(documentIds.length, results);

        return scoredDocuments;
    }

    private void assertNumberOfResults(int numberOfResults, Results<ScoredDocument> results) {
        assertEquals("number of found documents", numberOfResults, results.getNumberFound());
        assertEquals("number of returned documents", numberOfResults, results.getNumberReturned());
        assertEquals("actual number of ScoredDcuments", numberOfResults, results.getResults().size());
    }

    @Ignore("No stemming yet")
    @Test
    public void testSearchByWordStem() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("rolling trolled")));
        index.add(newDocument("foobbb", newField("foo").setText("bowling")));

        assertSearchYields(index, "foo:roll", "fooaaa");
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectIndex() {
        Index fooIndex = getIndex("fooIndex");
        fooIndex.add(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = getIndex("barIndex");
        barIndex.add(newDocument("bar", newField("foo").setText("aaa")));

        assertSearchYields(fooIndex, "foo:aaa", "foo");
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectNamespace() {
        Index fooIndex = SearchServiceFactory.getSearchService(FOO_NAMESPACE).getIndex(getIndexSpec("index", Consistency.GLOBAL));
        fooIndex.add(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = SearchServiceFactory.getSearchService(BAR_NAMESPACE).getIndex(getIndexSpec("index", Consistency.GLOBAL));
        barIndex.add(newDocument("bar", newField("foo").setText("aaa")));

        assertSearchYields(fooIndex, "foo:aaa", "foo");
    }

    @Test
    public void testSearchOnAllFields() {
        Index index = getTestIndex();
        index.add(newDocument(newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.add(newDocument(newField("foo").setText("bbb"), newField("bar").setText("aaa")));
        index.add(newDocument(newField("foo").setText("bbb"), newField("bar").setText("bbb")));

        assertEquals(2, index.search("aaa").getResults().size());
    }

    @Test
    public void testComplexSearch1() {
        Index index = getTestIndex();
        index.add(newDocument("bm", newField("author").setText("Bob Marley")));
        index.add(newDocument("rj", newField("author").setText("Rose Jones")));
        index.add(newDocument("rt", newField("author").setText("Rose Trunk")));
        index.add(newDocument("tj", newField("author").setText("Tom Jones")));

        assertSearchYields(index, "author:(bob OR ((rose OR tom) AND jones))", "bm", "rj", "tj");
    }

    @Test
    public void testSearchWithNegation() {

        Index index = getTestIndex();
        index.add(newDocument("with_baz", newField("body").setText("Foo bar baz")));
        index.add(newDocument("without_baz", newField("body").setText("Foo bar.")));

        assertSearchYields(index, "body:foo AND NOT body:baz", "without_baz");
        assertSearchYields(index, "body:foo NOT body:baz", "without_baz");
    }

    @Test
    public void testSearchWithDisjunctionAndNegation() {

        Index index = getTestIndex();
        index.add(newDocument("foo_with_baz", newField("body").setText("Foo bar baz")));
        index.add(newDocument("foo_without_baz", newField("body").setText("Foo bar.")));
        index.add(newDocument("without_foo_without_baz", newField("body").setText("bar.")));
        index.add(newDocument("without_foo_with_baz", newField("body").setText("bar baz.")));

        assertSearchYields(index, "body:foo OR NOT body:baz", "foo_with_baz", "foo_without_baz", "without_foo_without_baz");
    }

    @Test
    public void testSearchWithNegationOnly() {
        Index index = getTestIndex();
        index.add(newDocument("with_baz", newField("body").setText("Foo bar baz")));
        index.add(newDocument("without_baz", newField("body").setText("Foo bar.")));

        assertSearchYields(index, "NOT body=baz", "without_baz");
    }

    @Test
     public void testGet() {
        Index index = getTestIndex();
        AddResponse ar = index.add(newDocument("get_id", newField("acme").setText("bipbip")));
        List<String> ids = ar.getIds();
        Assert.assertNotNull(ids);
        Assert.assertFalse(ids.isEmpty());
        Assert.assertNotNull(index.get(ids.get(0)));
    }
}
