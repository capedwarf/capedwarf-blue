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

package org.jboss.test.capedwarf.search.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.ScoredDocument;
import org.jboss.test.capedwarf.common.support.All;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(All.class)
public class SearchTest extends SearchTestBase {

    @Test
    public void testEmptyQueryReturnsAllDocuments() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.put(newDocument("foobbb", newField("bar").setText("bbb")));
        assertSearchYields(index, "", "fooaaa", "foobbb");
    }

    @Test
    public void testLeadingAndTrailingWhitespaceInQueryIsIgnored() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb")));

        assertSearchYields(index, "    ", "fooaaa", "foobbb");
        assertSearchYields(index, "\t\n", "fooaaa", "foobbb");

        assertSearchYields(index, "   foo:aaa       ", "fooaaa");
        assertSearchYields(index, "\tfoo:aaa\t", "fooaaa");
        assertSearchYields(index, "\nfoo:aaa\n", "fooaaa");
        assertSearchYields(index, "   \n foo:aaa \n ", "fooaaa");
    }

    @Test
    public void testSearchBySingleField() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb")));

        assertSearchYields(index, "foo:aaa", "fooaaa");
    }

    @Test
    public void testSearchByTwoFields() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.put(newDocument("foobbb", newField("foo").setText("aaa"), newField("bar").setText("ccc")));

        assertSearchYields(index, "foo:aaa bar:bbb", "fooaaa");
    }

    @Test
    public void testSearchDisjunction() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb"), newField("bar").setText("ccc")));
        index.put(newDocument("fooccc", newField("foo").setText("ccc"), newField("bar").setText("bbb")));

        assertSearchYields(index, "foo:aaa OR bar:bbb", "fooaaa", "fooccc");
    }

    @Test
    public void testSearchByTerm() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("bar aaa baz")));
        index.put(newDocument("foobbb", newField("foo").setText("bar bbb baz")));
        index.put(newDocument("fooaaa2", newField("foo").setText("baraaabaz")));

        assertSearchYields(index, "foo:aaa", "fooaaa");
    }

    @Test
    public void testSearchByPhrase() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa bbb ccc ddd")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb ccc aaa ddd")));

        assertSearchYields(index, "foo:\"aaa bbb ccc\"", "fooaaa");
    }

    @Test
    public void testTextWithCommonWord() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("text with num 0")));   // "with" is a very common word
        index.put(newDocument("foobbb", newField("foo").setText("text with num 1")));

        assertSearchYields(index, "text with num", "fooaaa", "foobbb");
    }

    @Test
    public void testSearchByStringEquality() {
        Index index = getTestIndex();
        index.put(newDocument("d", newField("foo").setText("ddd")));
        index.put(newDocument("b", newField("foo").setText("bbb")));
        index.put(newDocument("c", newField("foo").setText("ccc")));
        index.put(newDocument("a", newField("foo").setText("aaa")));

        assertSearchYields(index, "foo = bbb", "b");
    }

    @Test
    public void testSearchByDateEqualityAndInequality() {
        if (isRunningInsideGaeDevServer()) {
            // this test only works on dev server if the _system_ timezone is set to UTC
            return;
        }

        Index index = getTestIndex();
        index.put(newDocument("d", newField("date").setDate(createDate(2004, 5, 5))));
        index.put(newDocument("b", newField("date").setDate(createDate(2002, 5, 5))));
        index.put(newDocument("c", newField("date").setDate(createDate(2003, 5, 5))));
        index.put(newDocument("a", newField("date").setDate(createDate(2001, 5, 5))));

        assertSearchYields(index, "date > 2002-05-05", "c", "d");
        assertSearchYields(index, "date >= 2002-05-05", "b", "c", "d");
        assertSearchYields(index, "date < 2002-05-05", "a");
        assertSearchYields(index, "date <= 2002-05-05", "a", "b");
        assertSearchYields(index, "date = 2002-05-05", "b");
    }

    @Test
    public void testSearchByNumberEqualityAndInequality() {
        Index index = getTestIndex();
        index.put(newDocument("d", newField("num").setNumber(4.0d)));
        index.put(newDocument("b", newField("num").setNumber(2.0d)));
        index.put(newDocument("c", newField("num").setNumber(3.0d)));
        index.put(newDocument("a", newField("num").setNumber(1.0d)));

        assertSearchYields(index, "num > 2", "c", "d");
        assertSearchYields(index, "num >= 2", "b", "c", "d");
        assertSearchYields(index, "num < 2", "a");
        assertSearchYields(index, "num <= 2", "a", "b");
        assertSearchYields(index, "num = 2", "b");
        assertSearchYields(index, "num:2", "b");
    }

    @Test
    public void testSearchOnHtmlFieldIgnoresTags() {
        Index index = getTestIndex();
        index.put(newDocument("a", newField("html").setHTML("<html><body>hello</body></html>")));
        index.put(newDocument("b", newField("html").setHTML("<html><body>body</body></html>")));

        assertSearchYields(index, "html:body", "b");
    }

    @Test
    public void testSearchForNumberInText() {
        Index index = getTestIndex();
        index.put(newDocument("a", newField("text").setText("Founded in 1993, Red Hat has its corporate headquarters in Raleigh, North Carolina with satellite offices worldwide.")));
        assertSearchYields(index, "text:1993", "a");
    }

    @Test
    public void testSearchForLocationWithinSpecifiedDistance() {
        if (isRunningInsideGaeDevServer()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.put(newDocument("a", newField("location").setGeoPoint(new GeoPoint(45.0, 15.0))));
        index.put(newDocument("b", newField("location").setGeoPoint(new GeoPoint(60.0, 40.0))));
        assertSearchYields(index, "distance(location, geopoint(45.0, 15.0)) < 1000", "a");
    }

    @Test
    public void testArgumentsOfDistanceFunctionCanBeSwapped() {
        if (isRunningInsideGaeDevServer()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.put(newDocument("a", newField("location").setGeoPoint(new GeoPoint(45.0, 15.0))));
        index.put(newDocument("b", newField("location").setGeoPoint(new GeoPoint(60.0, 40.0))));
        assertSearchYields(index, "distance(geopoint(45.0, 15.0), location) < 1000", "a");
    }

    @Test
    public void testSearchForLocationYieldsResultsInsideRadiusButNotInsideSquare() {
        if (isRunningInsideGaeDevServer()) {
            return; // dev appengine does not support geo points
        }
        Index index = getTestIndex();
        index.put(newDocument("a", newField("location").setGeoPoint(new GeoPoint(46.051464, 14.515833))));
        index.put(newDocument("b", newField("location").setGeoPoint(new GeoPoint(46.046111, 14.513889))));
        assertSearchYields(index, "distance(location, geopoint(46.051464, 14.506097)) < 800", "a");
    }

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        //noinspection MagicConstant
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.clear(Calendar.MILLISECOND);
        return cal.getTime();
    }

    @Ignore("No stemming yet")
    @Test
    public void testSearchByWordStem() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("rolling trolled")));
        index.put(newDocument("foobbb", newField("foo").setText("bowling")));

        assertSearchYields(index, "foo:roll", "fooaaa");
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectIndex() {
        Index fooIndex = getIndex("fooIndex");
        fooIndex.put(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = getIndex("barIndex");
        barIndex.put(newDocument("bar", newField("foo").setText("aaa")));

        assertSearchYields(fooIndex, "foo:aaa", "foo");
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectNamespace() {
        Index fooIndex = getSearchService(FOO_NAMESPACE).getIndex(getIndexSpec("index"));
        fooIndex.put(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = getSearchService(BAR_NAMESPACE).getIndex(getIndexSpec("index"));
        barIndex.put(newDocument("bar", newField("foo").setText("aaa")));

        assertSearchYields(fooIndex, "foo:aaa", "foo");
    }

    @Test
    public void testSearchOnAllFields() {
        Index index = getTestIndex();
        index.put(newDocument(newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.put(newDocument(newField("foo").setText("bbb"), newField("bar").setText("aaa")));
        index.put(newDocument(newField("foo").setText("bbb"), newField("bar").setText("bbb")));

        assertEquals(2, index.search("aaa").getResults().size());
    }

    @Test
    public void testComplexSearch1() {
        Index index = getTestIndex();
        index.put(newDocument("bm", newField("author").setText("Bob Marley")));
        index.put(newDocument("rj", newField("author").setText("Rose Jones")));
        index.put(newDocument("rt", newField("author").setText("Rose Trunk")));
        index.put(newDocument("tj", newField("author").setText("Tom Jones")));

        assertSearchYields(index, "author:(bob OR ((rose OR tom) AND jones))", "bm", "rj", "tj");
    }

    @Test
    public void testSearchWithNegation() {

        Index index = getTestIndex();
        index.put(newDocument("with_baz", newField("body").setText("Foo bar baz")));
        index.put(newDocument("without_baz", newField("body").setText("Foo bar.")));

        assertSearchYields(index, "body:foo AND NOT body:baz", "without_baz");
        assertSearchYields(index, "body:foo NOT body:baz", "without_baz");
    }

    @Test
    public void testSearchWithDisjunctionAndNegation() {

        Index index = getTestIndex();
        index.put(newDocument("foo_with_baz", newField("body").setText("Foo bar baz")));
        index.put(newDocument("foo_without_baz", newField("body").setText("Foo bar.")));
        index.put(newDocument("without_foo_without_baz", newField("body").setText("bar.")));
        index.put(newDocument("without_foo_with_baz", newField("body").setText("bar baz.")));

        assertSearchYields(index, "body:foo OR NOT body:baz", "foo_with_baz", "foo_without_baz", "without_foo_without_baz");
    }

    @Test
    public void testSearchWithNegationOnly() {
        Index index = getTestIndex();
        index.put(newDocument("with_baz", newField("body").setText("Foo bar baz")));
        index.put(newDocument("without_baz", newField("body").setText("Foo bar.")));

        assertSearchYields(index, "NOT body=baz", "without_baz");
    }

    @Test
    public void testGet() {
        Index index = getTestIndex();
        PutResponse ar = index.put(newDocument("get_id", newField("acme").setText("bipbip")));
        List<String> ids = ar.getIds();
        Assert.assertNotNull(ids);
        Assert.assertFalse(ids.isEmpty());
        Assert.assertNotNull(index.get(ids.get(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOnlyField() {
        Index index = getTestIndex();
        index.put(newDocument("get_only_field", newField("foo").setText("foo"), newField("foo").setText("bar")));

        for (ScoredDocument document : index.search("").getResults()) {
            document.getOnlyField("foo");
        }
    }


}
