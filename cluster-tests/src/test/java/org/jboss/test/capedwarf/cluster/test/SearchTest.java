package org.jboss.test.capedwarf.cluster.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class SearchTest extends ClusteredTestBase {

    private SearchService service;

    @Before
    public void setUp() throws Exception {
        service = SearchServiceFactory.getSearchService();
    }

    @InSequence(1)
    @Test
    @OperateOnDeployment("dep1")
    public void cleanpOnStart() {
        clear();
        Index index = getTestIndex();
        List<Document> documents = getAllDocumentsIn(index );
        assertEquals(0, documents.size());
    }

    @InSequence(2)
    @Test
    @OperateOnDeployment("dep1")
    public void testTwoDocumentsHaveDistinctGeneratedIdsOnDep1() {
        Document doc1 = newEmptyDocument();   // id-less document
        Document doc2 = newEmptyDocument();   // id-less document

        Index index = getTestIndex();
        index.put(doc1, doc2);

        waitForSync();
        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(2, documents.size());
        assertFalse(documents.get(0).getId().equals(documents.get(1).getId()));
    }

    @InSequence(3)
    @Test
    @OperateOnDeployment("dep2")
    public void testTwoDocumentsHaveDistinctGeneratedIdsOnDep2() {
        Index index = getTestIndex();

        Document doc = newEmptyDocument();   // id-less document
        index.put(doc);

        waitForSync();

        List<Document> documents = getAllDocumentsIn(index);
        assertEquals(3, documents.size());
        assertFalse(documents.get(0).getId().equals(documents.get(1).getId()));
        assertFalse(documents.get(0).getId().equals(documents.get(2).getId()));
        assertFalse(documents.get(1).getId().equals(documents.get(2).getId()));
    }

    @InSequence(4)
    @Test
    @OperateOnDeployment("dep1")
    public void testAddOverwritesPreviousDocumentWithSameIdOnDep1() {
        clear();

        String documentId = "foo";

        Index index = getTestIndex();

        Document doc1 = Document.newBuilder()
            .setId(documentId)
            .setRank(123)
            .addField(newField("bar").setText("ding"))
            .build();
        index.put(doc1);

        Document doc2 = Document.newBuilder()
            .setId(documentId)
            .setRank(456)
            .addField(newField("bar").setText("dong"))
            .build();
        index.put(doc2);

        waitForSync();

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(doc2, retrievedDoc);
        assertEquals(456, retrievedDoc.getRank());
        assertEquals("dong", retrievedDoc.getOnlyField("bar").getText());
    }

    @InSequence(5)
    @Test
    @OperateOnDeployment("dep2")
    public void testAddOverwritesPreviousDocumentWithSameIdOnDep2() {
        waitForSync();

        String documentId = "foo";

        Index index = getTestIndex();
        List<Document> results = getAllDocumentsIn(index);
        Assert.assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(456, retrievedDoc.getRank());
        assertEquals("dong", retrievedDoc.getOnlyField("bar").getText());

        Document doc1 = Document.newBuilder()
                .setId(documentId)
                .setRank(789)
                .addField(newField("bar").setText("dooong"))
                .build();
        index.put(doc1);

        waitForSync();

        results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        retrievedDoc = results.get(0);
        assertEquals(doc1, retrievedDoc);
        assertEquals(789, retrievedDoc.getRank());
        assertEquals("dooong", retrievedDoc.getOnlyField("bar").getText());
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void testSearchBySingleFieldOnDep1() throws Exception {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb")));

        waitForSync();

        assertSearchYields(index, "foo:aaa", "fooaaa");
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testSearchBySingleFieldOnDep2() throws Exception {
        Index index = getTestIndex();
        index.put(newDocument("fooccc", newField("foo").setText("ccc")));

        waitForSync();
        assertSearchYields(index, "foo:bbb", "foobbb");
        assertSearchYields(index, "foo:ccc", "fooccc");

        clear();
    }

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void testSearchDisjunctionOnDep1() {
        Index index = getTestIndex();
        index.put(newDocument("fooaaa", newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.put(newDocument("foobbb", newField("foo").setText("bbb"), newField("bar").setText("ccc")));
        index.put(newDocument("fooccc", newField("foo").setText("ccc"), newField("bar").setText("bbb")));

        waitForSync();
        assertSearchYields(index, "foo:aaa OR bar:bbb", "fooaaa", "fooccc");
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep2")
    public void testSearchDisjunctionOnDep2() {
        Index index = getTestIndex();
        assertSearchYields(index, "foo:aaa OR bar:bbb", "fooaaa", "fooccc");

        index.put(newDocument("fooddd", newField("foo").setText("ddd"), newField("bar").setText("bbb")));

        waitForSync();
        assertSearchYields(index, "foo:aaa OR bar:bbb", "fooaaa", "fooccc", "fooddd");
    }

    @InSequence(50)
    @Test
    @OperateOnDeployment("dep1")
    public void testSearchReturnsDocumentsInCorrectIndexOnDep1() {
        Index fooIndex = getIndex("fooIndex");
        fooIndex.put(newDocument("foo", newField("foo").setText("aaa")));

        Index barIndex = getIndex("barIndex");
        barIndex.put(newDocument("bar", newField("foo").setText("aaa")));

        waitForSync();
        assertSearchYields(fooIndex, "foo:aaa", "foo");
    }

    @InSequence(60)
    @Test
    @OperateOnDeployment("dep2")
    public void testSearchReturnsDocumentsInCorrectIndexOnDep2() {
        Index fooIndex = getIndex("fooIndex");
        assertSearchYields(fooIndex, "foo:aaa", "foo");

        Index barIndex = getIndex("barIndex");
        assertSearchYields(barIndex, "foo:aaa", "bar");

        fooIndex.put(newDocument("foobbb", newField("foo").setText("aaa")));

        waitForSync();
        assertSearchYields(fooIndex, "foo:aaa", "foo", "foobbb");
    }

    @InSequence(70)
    @Test
    @OperateOnDeployment("dep1")
    public void testSearchOnAllFieldsOnDep1() {
        clear();
        Index index = getTestIndex();
        index.put(newDocument(newField("foo").setText("aaa"), newField("bar").setText("bbb")));
        index.put(newDocument(newField("foo").setText("bbb"), newField("bar").setText("aaa")));
        index.put(newDocument(newField("foo").setText("bbb"), newField("bar").setText("bbb")));

        waitForSync();
        assertEquals(2, index.search("aaa").getResults().size());
    }

    @InSequence(80)
    @Test
    @OperateOnDeployment("dep2")
    public void testSearchOnAllFieldsOnDep2() {
        Index index = getTestIndex();
        assertEquals(2, index.search("aaa").getResults().size());

        index.put(newDocument(newField("foo").setText("aaa"), newField("bar").setText("aaa")));
        index.put(newDocument(newField("foo").setText("aaa"), newField("bar").setText("ccc")));
        index.put(newDocument(newField("foo").setText("ccc"), newField("bar").setText("ccc")));
        waitForSync();
        assertEquals(4, index.search("aaa").getResults().size());
    }

    @InSequence(90)
    @Test
    @OperateOnDeployment("dep1")
    public void testComplexSearch1OnDep1() {
        Index index = getTestIndex();
        index.put(newDocument("bm", newField("author").setText("Bob Marley")));
        index.put(newDocument("rj", newField("author").setText("Rose Jones")));
        index.put(newDocument("rt", newField("author").setText("Rose Trunk")));
        index.put(newDocument("tj", newField("author").setText("Tom Jones")));

        waitForSync();
        assertSearchYields(index, "author:(bob OR ((rose OR tom) AND jones))", "bm", "rj", "tj");
    }

    @InSequence(100)
    @Test
    @OperateOnDeployment("dep2")
    public void testComplexSearch1OnDep2() {
        Index index = getTestIndex();
        assertSearchYields(index, "author:(bob OR ((rose OR tom) AND jones))", "bm", "rj", "tj");

        index.put(newDocument("zm", newField("author").setText("Ziggy Marley")));
        index.put(newDocument("bd", newField("author").setText("Bob Dylan")));

        waitForSync();
        assertSearchYields(index, "author:(bob OR ((rose OR tom) AND jones))", "bm", "rj", "tj", "bd");
    }


    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void tearDownOnDep1() throws Exception {
        clear();
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep2")
    public void tearDownOnDep2() throws Exception {
        clear();
    }

    private void assertSearchYields(Index index, String queryString, String... documentIds) {
        Results<ScoredDocument> results = index.search(queryString);
        Collection<ScoredDocument> scoredDocuments = results.getResults();
//        System.out.println("-------------------------------");
//        System.out.println("queryString = " + queryString);
//        System.out.println("scoredDocuments = " + scoredDocuments);
//        for (ScoredDocument scoredDocument : scoredDocuments) {
//            System.out.println("scoredDocument = " + scoredDocument);
//        }
        assertEquals("number of found documents", documentIds.length, results.getNumberFound());
        assertEquals("number of returned documents", documentIds.length, results.getNumberReturned());
        assertEquals("actual number of ScoredDcuments", documentIds.length, scoredDocuments.size());

        Set<String> expectedDocumentIds = new HashSet<String>(Arrays.asList(documentIds));
        for (ScoredDocument scoredDocument : scoredDocuments) {
            boolean wasContained = expectedDocumentIds.remove(scoredDocument.getId());
            if (!wasContained) {
                fail("Search \"" + queryString + "\" yielded unexpected document id: " + scoredDocument.getId());
            }
        }
    }

    private Index getTestIndex() {
        return getIndex("testIndex");
    }

    private Index getIndex(String name) {
        IndexSpec indexSpec = getIndexSpec(name);
        return service.getIndex(indexSpec);
    }

    private IndexSpec getIndexSpec(String name) {
        return IndexSpec.newBuilder().setName(name).build();
    }

    private Field.Builder newField(String fieldName) {
        return Field.newBuilder().setName(fieldName);
    }

    private Document newEmptyDocument() {
        return Document.newBuilder().build();
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

    private List<Document> getAllDocumentsIn(Index index) {
        return index.getRange(defaultGetRequest()).getResults();
    }

    protected GetRequest defaultGetRequest() {
        return GetRequest.newBuilder().build();
    }

    private void clear() {
        GetResponse<Index> response = service.getIndexes(GetIndexesRequest.newBuilder());
        for (Index index : response.getResults()) {
            GetResponse<Document> documents = index.getRange(GetRequest.newBuilder());
            for (Document document : documents.getResults()) {
                index.delete(document.getId());
            }
        }
    }

    private void waitForSync() {
        sync();
    }

}
