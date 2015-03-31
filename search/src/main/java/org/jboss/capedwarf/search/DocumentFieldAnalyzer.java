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
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.hibernate.search.util.impl.PassThroughAnalyzer;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public final class DocumentFieldAnalyzer extends DelegatingAnalyzerWrapper {

    private FieldNamePrefixer fieldNamePrefixer = new FieldNamePrefixer();

    public static final StandardHtmlAnalyzer STANDARD_HTML_ANALYZER = new StandardHtmlAnalyzer();
    public static final StandardAnalyzer STANDARD_ANALYZER = new StandardAnalyzer(CharArraySet.EMPTY_SET);
    public static final PassThroughAnalyzer PASS_THROUGH_ANALYZER = PassThroughAnalyzer.INSTANCE;

    public DocumentFieldAnalyzer() {
        super(GLOBAL_REUSE_STRATEGY);
    }

    protected Analyzer getWrappedAnalyzer(String fieldName) {
        Field.FieldType fieldType = fieldNamePrefixer.getFieldType(fieldName);
        return getAnalyzer(fieldType);
    }

    private Analyzer getAnalyzer(Field.FieldType fieldType) {
        switch (fieldType) {
            case NUMBER:
                return PASS_THROUGH_ANALYZER;
            case TEXT:
                return STANDARD_ANALYZER;
            case HTML:
                return STANDARD_HTML_ANALYZER;
            case ATOM:
                return PASS_THROUGH_ANALYZER;
            case DATE:
                return PASS_THROUGH_ANALYZER;
            case GEO_POINT:
                return PASS_THROUGH_ANALYZER;
            default:
                throw new IllegalArgumentException("Unsupported field type: " + fieldType);

        }
    }
}
