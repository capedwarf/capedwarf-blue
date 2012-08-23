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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.google.appengine.api.search.AddResponse;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListRequest;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import org.junit.Test;

import static com.google.appengine.api.search.Field.FieldType.TEXT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BasicTestCase extends AbstractTest {

    @Test
    public void testGetSearchServiceWithNamespace() {
        SearchService service = SearchServiceFactory.getSearchService("foo");
        assertEquals("foo", service.getNamespace());
    }

    @Test
    public void testIndexInheritsNamespaceFromSearchService() {
        SearchService service = SearchServiceFactory.getSearchService("foo");
        Index index = service.getIndex(IndexSpec.newBuilder().build());
        assertEquals("foo", index.getNamespace());
    }

    @Test
    public void testIndexHasRequestedConsistency() {
        assertEquals(Consistency.GLOBAL, getIndex("foo").getConsistency());
        assertEquals(Consistency.PER_DOCUMENT, getIndex("bar", Consistency.PER_DOCUMENT).getConsistency());
    }

    @Test
    public void testIndexHasRequestedName() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName("bar").build();
        assertEquals("bar", service.getIndex(indexSpec).getName());
    }

    @Test
    public void testAddDocument() {
        Document doc = newEmptyDocument("foo");

        Index index = getTestIndex();
        index.add(doc);

        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(1, documents.size());
        assertEquals(doc, documents.get(0));
    }

    @Test
    public void testAddDocumentWithoutId() {
        Document doc = newEmptyDocument();   // id-less document

        Index index = getTestIndex();
        AddResponse addResponse = index.add(doc);

        assertNull(doc.getId()); // id must stay null

        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(1, documents.size());
        assertEquals(addResponse.getIds().get(0), documents.get(0).getId());
        assertNotNull(documents.get(0).getId());
    }

    @Test
    public void testTwoDocumentsHaveDistinctGeneratedIds() {
        Document doc1 = newEmptyDocument();   // id-less document
        Document doc2 = newEmptyDocument();   // id-less document

        Index index = getTestIndex();
        index.add(doc1, doc2);

        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(2, documents.size());
        assertFalse(documents.get(0).getId().equals(documents.get(1).getId()));
    }

    @Test
    public void testAddOverwritesPreviousDocumentWithSameId() {
        String documentId = "foo";

        Index index = getTestIndex();

        Document doc1 = Document.newBuilder()
            .setId(documentId)
            .setRank(123)
            .addField(newField("bar").setText("ding"))
            .build();
        index.add(doc1);

        Document doc2 = Document.newBuilder()
            .setId(documentId)
            .setRank(456)
            .addField(newField("bar").setText("dong"))
            .build();
        index.add(doc2);

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(doc2, retrievedDoc);
        assertEquals(456, retrievedDoc.getRank());
        assertEquals("dong", retrievedDoc.getOnlyField("bar").getText());
    }

    @Test
    public void testAddedDocumentIsStoredOnlyInTheIndexItWasAddedTo() {
        Index fooIndex = getIndex("fooIndex");
        Index barIndex = getIndex("barIndex");

        fooIndex.add(newEmptyDocument("foo"));

        assertEquals(1, numberOfDocumentsIn(fooIndex));
        assertEquals(0, numberOfDocumentsIn(barIndex));
    }

    @Test
    public void testAddedDocumentIsStoredInCorrectNamespace() {
        SearchService fooService = SearchServiceFactory.getSearchService(FOO_NAMESPACE);
        SearchService barService = SearchServiceFactory.getSearchService(BAR_NAMESPACE);

        Index fooNamespaceIndex = fooService.getIndex(getIndexSpec("index", Consistency.GLOBAL));
        Index barNamespaceIndex = barService.getIndex(getIndexSpec("index", Consistency.GLOBAL));

        fooNamespaceIndex.add(newEmptyDocument("foo"));

        assertEquals(1, numberOfDocumentsIn(fooNamespaceIndex));
        assertEquals(0, numberOfDocumentsIn(barNamespaceIndex));
    }

    @Test
    public void testDocumentRetainsCoreProperties() {
        Document doc = Document.newBuilder()
            .setId("foo")
            .setLocale(Locale.CANADA)
            .setRank(123)
            .build();

        Document retrievedDoc = addAndRetrieve(doc);
        assertEquals("document id is not persisted", "foo", retrievedDoc.getId());
        if (!runningInsideDevAppEngine()) {
            assertEquals("document locale is not persisted", Locale.CANADA, retrievedDoc.getLocale());
        }
        assertEquals("document rank is not persisted", 123, retrievedDoc.getRank());
    }

    @Test
    public void testDocumentHandlesTextFields() {
        Document doc = newDocument(newField("textField").setText("text"));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("textField");
        assertEquals(TEXT, field.getType());
        assertEquals("text", field.getText());
    }

    @Test
    public void testDocumentHandlesHtmlFields() {
        Document doc = newDocument(newField("htmlField").setHTML("<html><body>text</body></html>"));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("htmlField");
        assertEquals(Field.FieldType.HTML, field.getType());
        assertEquals("<html><body>text</body></html>", field.getHTML());
    }

    @Test
    public void testDocumentHandlesAtomFields() {
        Document doc = newDocument(newField("atomField").setAtom("atom"));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("atomField");
        assertEquals(Field.FieldType.ATOM, field.getType());
        assertEquals("atom", field.getAtom());
    }

    @Test
    public void testDocumentHandlesNumberFields() {
        Document doc = newDocument(newField("numberField").setNumber(123.0));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("numberField");
        assertEquals(Field.FieldType.NUMBER, field.getType());
        assertEquals(123.0, field.getNumber());
    }

    @Test
    public void testDocumentHandlesDateFields() {
        Date today = Field.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Document doc = newDocument(newField("dateField").setDate(today));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("dateField");
        assertEquals(Field.FieldType.DATE, field.getType());
        assertEquals(Field.truncate(today, Calendar.DAY_OF_MONTH), retrievedDoc.getOnlyField("dateField").getDate());
    }

    @Test
    public void testDocumentHandlesGeoPointFields() {
        if (runningInsideDevAppEngine()) {
            return; // dev appengine does not support geo points
        }
        Document doc = newDocument(newField("geoPointField").setGeoPoint(new GeoPoint(45.0, 15.0)));
        Document retrievedDoc = addAndRetrieve(doc);
        Field field = retrievedDoc.getOnlyField("geoPointField");
        assertEquals(Field.FieldType.GEO_POINT, field.getType());
        assertEquals(45.0, field.getGeoPoint().getLatitude());
        assertEquals(15.0, field.getGeoPoint().getLongitude());
    }

    @Test
    public void testDocumentRetainsMultipleFieldsOfSameName() {
        Document doc = Document.newBuilder()
            .setId("foo")
            .addField(newField("field").setText("text"))
            .addField(newField("field").setText("anotherText"))
            .addField(newField("field").setAtom("atom"))
            .build();

        Document retrievedDoc = addAndRetrieve(doc);
        assertEquals(3, retrievedDoc.getFieldCount("field"));
        assertDocumentContainsField(retrievedDoc, "field", Field.FieldType.TEXT, "text");
        assertDocumentContainsField(retrievedDoc, "field", Field.FieldType.TEXT, "anotherText");
        assertDocumentContainsField(retrievedDoc, "field", Field.FieldType.ATOM, "atom");
    }

    @Test
    public void testAddMultipleDocuments() {
        Document foo = newEmptyDocument("foo");
        Document bar = newEmptyDocument("bar");
        Document baz = newEmptyDocument("baz");

        Index index = getTestIndex();
        index.add(foo, bar, baz);

        assertEquals(
            new HashSet<Document>(Arrays.asList(foo, bar, baz)),
            new HashSet<Document>(getAllDocumentsIn(index)));
    }


    @Test
    public void testAddMultipleDocumentsThroughIterable() {
        Document foo = newEmptyDocument("foo");
        Document bar = newEmptyDocument("bar");
        Document baz = newEmptyDocument("baz");

        Index index = getTestIndex();
        index.add(Arrays.asList(foo, bar, baz));

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(
            new HashSet<Document>(Arrays.asList(foo, bar, baz)),
            new HashSet<Document>(results));
    }

    @Test
    public void testListDocumentsWithLimit() {
        Index index = getTestIndex();
        createEmptyDocuments(10, index);

        ListRequest listRequest = ListRequest.newBuilder().setLimit(5).build();
        assertEquals(5, index.listDocuments(listRequest).getResults().size());
    }

    @Test
    public void testListDocumentsWithStartId() {
        Index index = getTestIndex();
        index.add(newEmptyDocument("aaa"));
        index.add(newEmptyDocument("bbb"));
        index.add(newEmptyDocument("ccc"));
        index.add(newEmptyDocument("ddd"));

        ListRequest listRequest1 = ListRequest.newBuilder().setStartId("bbb").setIncludeStart(true).build();
        assertListContainsDocumentsWithIds(Arrays.asList("bbb", "ccc", "ddd"), index.listDocuments(listRequest1).getResults());

        ListRequest listRequest2 = ListRequest.newBuilder().setStartId("bbb").setIncludeStart(false).build();
        assertListContainsDocumentsWithIds(Arrays.asList("ccc", "ddd"), index.listDocuments(listRequest2).getResults());
    }

    @Test
    public void testRemoveDocument() {
        Index index = getTestIndex();
        index.add(newEmptyDocument("foo"));
        index.add(newEmptyDocument("bar"));
        index.remove("foo", "bar");
        assertEquals(0, numberOfDocumentsIn(index));
    }

    @Test
    public void testRemoveDocumentIterable() {
        Index index = getTestIndex();
        index.add(newEmptyDocument("foo"));
        index.add(newEmptyDocument("bar"));
        index.remove(Arrays.asList("foo", "bar"));
        assertEquals(0, numberOfDocumentsIn(index));
    }

}
