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
import java.util.Locale;

import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListRequest;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.search.CapedwarfSearchService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.search.Field.FieldType.TEXT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 */
@RunWith(Arquillian.class)
public class SearchTestCase {

    private SearchService service;

    @Before
    public void setUp() throws Exception {
        service = SearchServiceFactory.getSearchService();
        ((CapedwarfSearchService)service).clear();
    }

    @After
    public void tearDown() throws Exception {
        ((CapedwarfSearchService)service).clear();
    }

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(SearchTestCase.class)
            .setWebXML(new StringAsset("<web/>"))
            .addAsWebInfResource("appengine-web.xml");
    }

    @Test
    public void testGetSearchService() {
        assertTrue(service instanceof CapedwarfSearchService);
        // TODO: check namespace
    }

    @Test
    public void testGetSearchServiceWithNamespace() {
        SearchService service = SearchServiceFactory.getSearchService("foo");
        assertTrue(service instanceof CapedwarfSearchService);
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
        assertEquals(Consistency.GLOBAL, getIndex("foo", Consistency.GLOBAL).getConsistency());
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
        index.add(doc);

        assertNotNull(doc.getId()); // TODO: check if document is really supposed to get the id (perhaps it must stay null)

        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(1, documents.size());
        assertEquals(doc, documents.get(0));
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
            .setOrderId(123)
            .addField(newField("bar").setText("ding"))
            .build();
        index.add(doc1);

        Document doc2 = Document.newBuilder()
            .setId(documentId)
            .setOrderId(456)
            .addField(newField("bar").setText("dong"))
            .build();
        index.add(doc2);

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(doc2, retrievedDoc);
        assertEquals(456, retrievedDoc.getOrderId());
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
        SearchService fooService = SearchServiceFactory.getSearchService("fooNamespace");
        SearchService barService = SearchServiceFactory.getSearchService("barNamespace");

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
            .setOrderId(123)
            .build();

        Document retrievedDoc = addAndRetrieve(doc);
        assertEquals("document id is not persisted", "foo", retrievedDoc.getId());
        assertEquals("document locale is not persisted", Locale.CANADA, retrievedDoc.getLocale());
        assertEquals("document orderId is not persisted", 123, retrievedDoc.getOrderId());
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

    private static void assertDocumentContainsField(Document doc, String fieldName, Field.FieldType fieldType, Object fieldValue) {
        for (Field field : doc.getField(fieldName)) {
            if (field.getType().equals(fieldType)) {
                Object storedValue = getFieldValue(field, fieldType);
                if (storedValue.equals(fieldValue)) {
                    return;
                }
            }
        }
        Assert.fail("Document does not contain field " + fieldName + " of type " + fieldType + " with value " + fieldValue);
    }

    private static Object getFieldValue(Field field, Field.FieldType fieldType) {
        switch (fieldType) {
            case TEXT:
                return field.getText();
            case ATOM:
                return field.getAtom();
            default:
                throw new InternalError("Not implemented");
        }
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

    private static void assertListContainsDocumentsWithIds(Collection<String> documentIds, List<Document> documents) {
        HashSet<String> ids = new HashSet<String>(documentIds);
        for (Document document : documents) {
            boolean removed = ids.remove(document.getId());
            if (!removed) {
                fail("list contained document with unexpected id: " + document.getId());
            }
        }
        if (!ids.isEmpty()) {
            fail("list did not contain the following ids: " + ids);
        }
    }

    private void createEmptyDocuments(int number, Index index) {
        for (int i=0; i<number; i++) {
            index.add(newEmptyDocument());
        }
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

    @Test
    public void testSearchBySingleField() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.add(newDocument("foobbb", newField("foo").setText("bbb")));

        Results<ScoredDocument> results = index.search("foo:aaa");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("fooaaa", scoredDocument.getId());
    }

    @Ignore("must implement conversion from GAE query string to Lucene query string")
    @Test
    public void testSearchByTwoFields() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.add(newDocument("foobbb", newField("foo").setText("aaa"), newField("bar").setText("ccc")));

        Results<ScoredDocument> results = index.search("foo:aaa bar:bbb");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("fooaaa", scoredDocument.getId());
    }

    @Test
    public void testSearchByTerm() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("bar aaa baz")));
        index.add(newDocument("foobbb", newField("foo").setText("bar bbb baz")));
        index.add(newDocument("fooaaa2", newField("foo").setText("baraaabaz")));

        Results<ScoredDocument> results = index.search("foo:aaa");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("fooaaa", scoredDocument.getId());
    }

    @Test
    public void testSearchByPhrase() {
        Index index = getTestIndex();
        index.add(newDocument("fooaaa", newField("foo").setText("aaa bbb ccc ddd")));
        index.add(newDocument("foobbb", newField("foo").setText("bbb ccc aaa ddd")));

        Results<ScoredDocument> results = index.search("foo:\"aaa bbb ccc\"");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("fooaaa", scoredDocument.getId());
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectIndex() {
        Index fooIndex = getIndex("fooIndex");
        fooIndex.add(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = getIndex("barIndex");
        barIndex.add(newDocument("bar", newField("foo").setText("aaa")));

        Results<ScoredDocument> results = fooIndex.search("foo:aaa");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("foo", scoredDocument.getId());
    }

    @Test
    public void testSearchReturnsDocumentsInCorrectNamespace() {
        Index fooIndex = SearchServiceFactory.getSearchService("fooNamespace").getIndex(getIndexSpec("index", Consistency.GLOBAL));
        fooIndex.add(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = SearchServiceFactory.getSearchService("barNamespace").getIndex(getIndexSpec("index", Consistency.GLOBAL));
        barIndex.add(newDocument("bar", newField("foo").setText("aaa")));

        Results<ScoredDocument> results = fooIndex.search("foo:aaa");
        assertEquals(1, results.getNumberFound());
        assertEquals(1, results.getNumberReturned());

        Collection<ScoredDocument> scoredDocuments = results.getResults();
        assertEquals(1, scoredDocuments.size());

        ScoredDocument scoredDocument = scoredDocuments.iterator().next();
        assertEquals("foo", scoredDocument.getId());
    }

    @Test
    public void testSearchOnAllFields() {
        Index index = getTestIndex();
        index.add(newDocument(newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.add(newDocument(newField("foo").setText("bbb"), newField("bar").setText("aaa")));
        index.add(newDocument(newField("foo").setText("bbb"), newField("bar").setText("bbb")));

        assertEquals(2, index.search("aaa").getResults().size());
    }

    private Document newEmptyDocument() {
        return Document.newBuilder().build();
    }

    private Document newEmptyDocument(String id) {
        return newDocument(id);
    }

    private Document newDocument(Field.Builder... fields) {
        return newDocument(null, fields);
    }

    private Document newDocument(String id, Field.Builder... fields) {
        Document.Builder builder = Document.newBuilder();
        if (id != null) {
            builder.setId(id);
        }
        for (Field.Builder field : fields) {
            builder.addField(field);
        }
        return builder.build();
    }

    private Field.Builder newField(String fieldName) {
        return Field.newBuilder().setName(fieldName);
    }

    private Index getTestIndex() {
        return getIndex("testIndex");
    }

    private Index getIndex(String name) {
        return getIndex(name, Consistency.GLOBAL);
    }

    private Index getIndex(String name, Consistency consistency) {
        IndexSpec indexSpec = getIndexSpec(name, consistency);
        return service.getIndex(indexSpec);
    }

    private IndexSpec getIndexSpec(String name, Consistency consistency) {
        return IndexSpec.newBuilder().setName(name).setConsistency(consistency).build();
    }

    private ListRequest defaultListRequest() {
        return ListRequest.newBuilder().build();
    }

    private Document addAndRetrieve(Document doc) {
        Index index = getTestIndex();
        index.add(doc);

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(doc, retrievedDoc);
        return retrievedDoc;
    }

    private int numberOfDocumentsIn(Index index) {
        return getAllDocumentsIn(index).size();
    }

    private List<Document> getAllDocumentsIn(Index index) {
        return index.listDocuments(defaultListRequest()).getResults();
    }

}
