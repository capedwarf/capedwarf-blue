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

import com.google.appengine.api.search.Field;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.util.impl.PassThroughAnalyzer;

import java.io.Reader;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DocumentFieldAnalyzer extends Analyzer {

    private FieldNamePrefixer fieldNamePrefixer = new FieldNamePrefixer();

    private StandardHtmlAnalyzer standardHtmlAnalyzer = new StandardHtmlAnalyzer(GAEQueryTreeVisitor.LUCENE_VERSION);
    private StandardAnalyzer standardAnalyzer = new StandardAnalyzer(GAEQueryTreeVisitor.LUCENE_VERSION);
    private PassThroughAnalyzer passThroughAnalyzer = new PassThroughAnalyzer();

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        Field.FieldType fieldType = fieldNamePrefixer.getFieldType(fieldName);
        Analyzer analyzer = getAnalyzer(fieldType);
        return analyzer.tokenStream(fieldName, reader);
    }

    private Analyzer getAnalyzer(Field.FieldType fieldType) {
        switch (fieldType) {
            case NUMBER:
                return passThroughAnalyzer;
            case TEXT:
                return standardAnalyzer;
            case HTML:
                return standardHtmlAnalyzer;
            case ATOM:
                return passThroughAnalyzer;
            case DATE:
                return passThroughAnalyzer;
            default:
                throw new IllegalArgumentException("Unsupported field type: " + fieldType);

        }
    }
}
