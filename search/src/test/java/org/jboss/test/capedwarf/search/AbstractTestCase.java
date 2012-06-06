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

import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListRequest;
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
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public abstract class AbstractTestCase {
    protected SearchService service;

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(AbstractTestCase.class)
            .setWebXML(new StringAsset("<web/>"))
            .addAsWebInfResource("appengine-web.xml");
    }

    @Before
    public void setUp() throws Exception {
        service = SearchServiceFactory.getSearchService();
        ((CapedwarfSearchService) service).clear();
    }

    @After
    public void tearDown() throws Exception {
        ((CapedwarfSearchService) service).clear();
    }


    protected static void assertDocumentContainsField(Document doc, String fieldName, Field.FieldType fieldType, Object fieldValue) {
        for (Field field : doc.getField(fieldName)) {
            if (field.getType().equals(fieldType)) {
                Object storedValue = getFieldValue(field, fieldType);
                if (storedValue.equals(fieldValue)) {
                    return;
                }
            }
        }
        fail("Document does not contain field " + fieldName + " of type " + fieldType + " with value " + fieldValue);
    }

    protected static Object getFieldValue(Field field, Field.FieldType fieldType) {
        switch (fieldType) {
            case TEXT:
                return field.getText();
            case ATOM:
                return field.getAtom();
            default:
                throw new InternalError("Not implemented");
        }
    }

    protected static void assertListContainsDocumentsWithIds(Collection<String> documentIds, List<Document> documents) {
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

    protected Document newEmptyDocument() {
        return Document.newBuilder().build();
    }

    protected Document newEmptyDocument(String id) {
        return newDocument(id);
    }

    protected Document newDocument(Field.Builder... fields) {
        return newDocument(null, fields);
    }

    protected Document newDocument(String id, Field.Builder... fields) {
        Document.Builder builder = Document.newBuilder();
        if (id != null) {
            builder.setId(id);
        }
        for (Field.Builder field : fields) {
            builder.addField(field);
        }
        return builder.build();
    }

    protected Field.Builder newField(String fieldName) {
        return Field.newBuilder().setName(fieldName);
    }

    protected Index getTestIndex() {
        return getIndex("testIndex");
    }

    protected Index getIndex(String name) {
        return getIndex(name, Consistency.GLOBAL);
    }

    protected Index getIndex(String name, Consistency consistency) {
        IndexSpec indexSpec = getIndexSpec(name, consistency);
        return service.getIndex(indexSpec);
    }

    protected IndexSpec getIndexSpec(String name, Consistency consistency) {
        return IndexSpec.newBuilder().setName(name).setConsistency(consistency).build();
    }

    protected ListRequest defaultListRequest() {
        return ListRequest.newBuilder().build();
    }

    protected Document addAndRetrieve(Document doc) {
        Index index = getTestIndex();
        index.add(doc);

        List<Document> results = getAllDocumentsIn(index);
        assertEquals(1, results.size());

        Document retrievedDoc = results.get(0);
        assertEquals(doc, retrievedDoc);
        return retrievedDoc;
    }

    protected int numberOfDocumentsIn(Index index) {
        return getAllDocumentsIn(index).size();
    }

    protected List<Document> getAllDocumentsIn(Index index) {
        return index.listDocuments(defaultListRequest()).getResults();
    }

    protected void createEmptyDocuments(int number, Index index) {
        for (int i=0; i<number; i++) {
            index.add(newEmptyDocument());
        }
    }
}
