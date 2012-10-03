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
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.query.ParserUtils;
import com.google.appengine.api.search.query.QueryLexer;
import com.google.appengine.api.search.query.QueryTreeContext;
import com.google.appengine.api.search.query.QueryTreeVisitor;
import com.google.appengine.repackaged.org.antlr.runtime.tree.Tree;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Version;
import org.hibernate.search.spatial.SpatialQueryBuilder;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class GAEQueryTreeVisitor implements QueryTreeVisitor<Context> {

    public static final Version LUCENE_VERSION = Version.LUCENE_36;

    private String allFieldName;

    public GAEQueryTreeVisitor(String allFieldName) {
        this.allFieldName = allFieldName;
    }

    public void visitSequence(Tree tree, Context context) {
        visitConjunction(tree, context); // "author:bob author:alice" is equivalent to "author:bob AND author:alice"
    }

    public void visitConjunction(Tree tree, Context context) {
        BooleanQuery booleanQuery = new BooleanQuery();

        walkThroughChildren(booleanQuery, BooleanClause.Occur.MUST, context);
        context.setQuery(booleanQuery);
    }

    public void visitDisjunction(Tree tree, Context context) {
        BooleanQuery booleanQuery = new BooleanQuery();
        walkThroughChildren(booleanQuery, BooleanClause.Occur.SHOULD, context);
        context.setQuery(booleanQuery);
    }

    public void visitNegation(Tree tree, Context context) {
        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(new TermQuery(new Term(CacheValue.MATCH_ALL_DOCS_FIELD_NAME, CacheValue.MATCH_ALL_DOCS_FIELD_VALUE)), BooleanClause.Occur.MUST);
        walkThroughChildren(booleanQuery, BooleanClause.Occur.MUST_NOT, context);
        context.setQuery(booleanQuery);
    }

    private void walkThroughChildren(BooleanQuery booleanQuery, BooleanClause.Occur occur, Context context) {
        for (Context childContext : context.children()) {
            booleanQuery.add(childContext.getQuery(), occur);
        }
    }

    public void visitFuzzy(Tree tree, Context context) {
        context.setRewriteMode(QueryTreeContext.RewriteMode.FUZZY);
    }

    public void visitLiteral(Tree tree, Context context) {
        context.setRewriteMode(QueryTreeContext.RewriteMode.STRICT);
    }

    public void visitLessThan(Tree tree, Context context) {
        visitOperator(context, Operator.LESS_THAN);
    }

    public void visitLessOrEqual(Tree tree, Context context) {
        visitOperator(context, Operator.LESS_OR_EQUAL);
    }

    public void visitGreaterThan(Tree tree, Context context) {
        visitOperator(context, Operator.GREATER_THAN);
    }

    public void visitGreaterOrEqual(Tree tree, Context context) {
        visitOperator(context, Operator.GREATER_OR_EQUAL);
    }

    public void visitEqual(Tree tree, Context context) {
        visitOperator(context, Operator.EQ);
    }

    public void visitContains(Tree tree, Context context) {
        visitOperator(context, Operator.CONTAINS);
    }

    protected void visitOperator(Context context, Operator operator) {
        context.setQuery(createComparisonQuery(context, operator));
    }

    public void visitValue(Tree tree, Context context) {
        Tree type = tree.getChild(0);
        Tree value = tree.getChild(1);

        if (type.getType() == QueryLexer.STRING) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < tree.getChildCount(); i++)
                builder.append(tree.getChild(i).getText());

            context.setText(builder.toString());
            context.setKind(QueryTreeContext.Kind.PHRASE);
            context.setReturnType(QueryTreeContext.Type.TEXT);
        } else if (type.getType() == QueryLexer.NUMBER) {
            context.setKind(QueryTreeContext.Kind.LITERAL);
            context.setReturnType(QueryTreeContext.Type.NUMBER);
        } else {
            String text = value.getText();
            context.setText(text);
            context.setKind(QueryTreeContext.Kind.LITERAL);
            context.setReturnType(ParserUtils.isNumber(text) ? QueryTreeContext.Type.NUMBER : QueryTreeContext.Type.TEXT);
        }
    }

    private Query createComparisonQuery(Context context, Operator operator) {
        Context leftSide = context.getChild(0);
        Context rightSide = context.getChild(1);

        if (leftSide.isFunction() || rightSide.isFunction()) {  // TODO
            Context function;
            Context value;

            if (leftSide.isFunction()) {
                function = leftSide;
                value = rightSide;
            } else {
                value = leftSide;
                function = rightSide;
            }

            if (!"distance".equals(function.getText())) {
                throw new IllegalArgumentException("Unsupported function " + leftSide.getText());
            }

            Context leftArgument = function.getChild(0);
            Context rightArgument = function.getChild(1);

            String fieldName = leftArgument.isCompatibleWith(QueryTreeContext.Type.LOCATION) ? rightArgument.getText() : leftArgument.getText();
            GeoPoint geoPoint = leftArgument.isCompatibleWith(QueryTreeContext.Type.LOCATION) ? leftArgument.getGeoPoint() : rightArgument.getGeoPoint();

            double latitude = geoPoint.getLatitude();
            double longitude = geoPoint.getLongitude();
            double radius = Double.parseDouble(value.getText()) / 1000; // need to convert from m to km

            String prefixedFieldName = new FieldNamePrefixer().getPrefixedFieldName(fieldName, Field.FieldType.GEO_POINT);
            return SpatialQueryBuilder.buildSpatialQueryByGrid(latitude, longitude, radius, prefixedFieldName);

        } else {
            return createQuery(leftSide.getText(), operator, rightSide);
        }
    }

    protected Query createQuery(String field, Operator operator, Context value) {
        if (value.isCompatibleWith(QueryTreeContext.Type.NUMBER)) {
            return createNumericQuery(field, operator, value);
        } else {
            return createTextQuery(field, operator, value);
        }
    }

    private Query createNumericQuery(String field, Operator operator, Context value) {
        double doubleValue = Double.parseDouble(value.getText());

        switch (operator) {
            case CONTAINS:
                return new TermQuery(new Term(field, value.getText()));
            case EQ:
                return NumericRangeQuery.newDoubleRange(field, doubleValue, doubleValue, true, true);
            case GREATER_THAN:
                return NumericRangeQuery.newDoubleRange(field, doubleValue, null, false, false);
            case GREATER_OR_EQUAL:
                return NumericRangeQuery.newDoubleRange(field, doubleValue, null, true, true);
            case LESS_THAN:
                return NumericRangeQuery.newDoubleRange(field, null, doubleValue, false, false);
            case LESS_OR_EQUAL:
                return NumericRangeQuery.newDoubleRange(field, null, doubleValue, true, true);
            default:
                // fail fast
                throw new RuntimeException("Unsupported operator: " + operator);
        }
    }

    protected Query createTextQuery(String field, Operator operator, Context text) {
        switch (operator) {
            case CONTAINS:
                return createContainsQuery(field, text);
            case EQ:
                return new TermRangeQuery(field, text.getText(), text.getText(), true, true);
            case GREATER_THAN:
                return new TermRangeQuery(field, text.getText(), null, false, false);
            case GREATER_OR_EQUAL:
                return new TermRangeQuery(field, text.getText(), null, true, true);
            case LESS_THAN:
                return new TermRangeQuery(field, null, text.getText(), false, false);
            case LESS_OR_EQUAL:
                return new TermRangeQuery(field, null, text.getText(), true, true);
            default:
                // fail fast
                throw new RuntimeException("Unsupported operator: " + operator);
        }
    }

    private Query createContainsQuery(String field, Context text) {
        if (text.isPhrase()) {
            try {
                return new QueryParser(LUCENE_VERSION, null, new StandardAnalyzer(LUCENE_VERSION)).parse(field + ":" + text.getText());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new TermQuery(new Term(field, text.getText().toLowerCase()));
        }
    }

    public void visitFunction(Tree tree, Context context) {
        String functionName = tree.getChild(0).getText();
        if ("geopoint".equals(functionName)) {
            context.setReturnType(QueryTreeContext.Type.LOCATION);
            context.setKind(QueryTreeContext.Kind.FUNCTION);
            context.setGeoPoint(new GeoPoint(Double.valueOf(context.getChild(0).getText()), Double.valueOf(context.getChild(1).getText())));
            context.setText("geopoint");
        } else if ("distance".equals(functionName)) {
            context.setReturnType(QueryTreeContext.Type.NUMBER);
            context.setKind(QueryTreeContext.Kind.FUNCTION);
            context.setText(functionName);
        }
        // TODO
    }

    public void visitGlobal(Tree tree, Context context) {
        context.setText(allFieldName);
    }

    public void visitOther(Tree tree, Context context) {
        throw new RuntimeException("should never come here");
    }

}
