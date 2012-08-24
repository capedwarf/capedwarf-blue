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

package org.jboss.capedwarf.search;

import com.google.appengine.api.search.Document;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.ProvidedId;
import org.hibernate.search.annotations.TermVector;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Indexed
@ProvidedId
@Analyzer(impl = DocumentFieldAnalyzer.class)
public class CacheValue implements Serializable {

    public static final String EMPTY_NAMESPACE = "_____EMPTY_NAMESPACE____";
    public static final String ID_FIELD_NAME = "__id__";
    public static final String RANK_FIELD_NAME = "__rank__";
    public static final String LOCALE_FIELD_NAME = "__locale__";
    public static final String ALL_FIELD_NAME = "__all__";
    public static final String MATCH_ALL_DOCS_FIELD_NAME = "__ALL_DOCS__";
    public static final String MATCH_ALL_DOCS_FIELD_VALUE = "all";

    private String indexName;
    private String namespace;
    private Document document;

    public CacheValue(String indexName, String namespace, Document document) {
        this.indexName = indexName;
        this.namespace = namespace;
        this.document = document;
    }

    @Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getIndexName() {
        return indexName;
    }

    @Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getNamespace() {
        return namespace.isEmpty() ? EMPTY_NAMESPACE : namespace;
    }

    @Field(name = ID_FIELD_NAME, analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getId() {
        return document.getId();
    }

    @Field(name = LOCALE_FIELD_NAME, analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    @FieldBridge(impl = LocaleBridge.class)
    public Locale getLocale() {
        return document.getLocale();
    }

    @Field(name = RANK_FIELD_NAME, analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public int getRank() {
        return document.getRank();
    }

    @Field(index = Index.YES, analyze = Analyze.YES, termVector = TermVector.YES)
    @FieldBridge(impl = DocumentFieldBridge.class)
    public Document getDocument() {
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheValue that = (CacheValue) o;

        if (!document.equals(that.document)) return false;
        if (!indexName.equals(that.indexName)) return false;
        if (!namespace.equals(that.namespace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = indexName.hashCode();
        result = 31 * result + namespace.hashCode();
        result = 31 * result + document.hashCode();
        return result;
    }
}
