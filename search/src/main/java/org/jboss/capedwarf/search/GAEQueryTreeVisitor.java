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

import com.google.appengine.api.search.query.QueryLexer;
import com.google.appengine.api.search.query.QueryTreeVisitor;
import com.google.appengine.api.search.query.QueryTreeWalker;
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

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class GAEQueryTreeVisitor implements QueryTreeVisitor<Context> {

    public static final Version LUCENE_VERSION = Version.LUCENE_36;

    public void visitSequence(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        visitConjunction(walker, tree, context); // "author:bob author:alice" is equivalent to "author:bob AND author:alice"
    }

    public void visitConjunction(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        BooleanQuery booleanQuery = new BooleanQuery();
        context.addSubQuery(booleanQuery);
        Context childContext = new Context(booleanQuery, context.getFieldName(), context.getOperator()) {
            public void addSubQuery(Query query) {
                ((BooleanQuery) getQuery()).add(query, BooleanClause.Occur.MUST);
            }

            @Override
            public void addNegatedSubQuery(Query query) {
                ((BooleanQuery) getQuery()).add(query, BooleanClause.Occur.MUST_NOT);
            }
        };
        walkThroughChildren(walker, tree, childContext);
    }

    public void visitDisjunction(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        BooleanQuery booleanQuery = new BooleanQuery();
        context.addSubQuery(booleanQuery);
        Context childContext = new Context(booleanQuery, context.getFieldName(), context.getOperator()) {
            public void addSubQuery(Query query) {
                ((BooleanQuery) getQuery()).add(query, BooleanClause.Occur.SHOULD);
            }

            @Override
            public void addNegatedSubQuery(Query query) {
                ((BooleanQuery) getQuery()).add(query, BooleanClause.Occur.MUST_NOT);
            }
        };
        walkThroughChildren(walker, tree, childContext);
    }

    public void visitNegation(QueryTreeWalker<Context> walker, Tree tree, final Context context) {
        Context childContext = new ForwardingContext(context) {
            @Override
            public void addSubQuery(Query query) {
                context.addNegatedSubQuery(query);
            }

            @Override
            public void addNegatedSubQuery(Query query) {
                context.addSubQuery(query);
            }
        };
        walker.walk(tree.getChild(0), childContext);
    }

    private void walkThroughChildren(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            Tree childTree = tree.getChild(i);
            walker.walk(childTree, context);
        }
    }

    public void visitRestriction(QueryTreeWalker<Context> walker, Tree tree, final Context context) {
        Context childContext = new ForwardingContext(context);

        if (tree.getChild(0).getType() == QueryLexer.GLOBAL) {
            childContext.setOnGlobalField(true);
            childContext.setFieldName(context.getFieldName());
        } else {
            childContext.setFieldName(tree.getChild(0).getText());
        }

        walker.walk(tree.getChild(1), childContext);
    }

    public void visitFuzzy(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        // TODO
    }

    public void visitLiteral(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        // TODO
    }

    public void visitLessThan(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.LESS_THAN));
    }

    public void visitLessOrEqual(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.LESS_OR_EQUAL));
    }

    public void visitGreaterThan(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.GREATER_THAN));
    }

    public void visitGreaterOrEqual(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.GREATER_OR_EQUAL));
    }

    public void visitEqual(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.EQ));
    }

    public void visitContains(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walker.walk(tree.getChild(0), newChildContext(context, Operator.CONTAINS));
    }

    private Context newChildContext(final Context context, Operator operator) {
        Context childContext = new ForwardingContext(context);
        if (context.isOnGlobalField() && operator == Operator.EQ) {
            childContext.setOperator(context.getOperator() == null ? Operator.CONTAINS : context.getOperator());
        } else {
            childContext.setOperator(operator);
        }
        return childContext;
    }

    public void visitValue(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        String field = context.getFieldName();
        Operator operator = context.getOperator();
        Tree type = tree.getChild(0);
        Tree value = tree.getChild(1);
        context.addSubQuery(createQuery(field, operator, type, value));
    }

    protected Query createQuery(String field, Operator operator, Tree type, Tree value) {
        if (type.getType() == QueryLexer.NUMBER) {
            return createNumericQuery(field, operator, value);
        } else {
            String text = value.getText().toLowerCase();
            return createQuery(field, operator, text, type);
        }
    }

    private Query createNumericQuery(String field, Operator operator, Tree value) {
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

    private Query createQuery(String field, Operator operator, String text, Tree type) {
        switch (operator) {
            case CONTAINS:
                return createContainsQuery(field, type, text);
            case EQ:
                return new TermRangeQuery(field, text, text, true, true);
            case GREATER_THAN:
                return new TermRangeQuery(field, text, null, false, false);
            case GREATER_OR_EQUAL:
                return new TermRangeQuery(field, text, null, true, true);
            case LESS_THAN:
                return new TermRangeQuery(field, null, text, false, false);
            case LESS_OR_EQUAL:
                return new TermRangeQuery(field, null, text, true, true);
            default:
                // fail fast
                throw new RuntimeException("Unsupported operator: " + operator);
        }
    }

    private Query createContainsQuery(String field, Tree type, String text) {
        if (type.getText().equals("WORD")) {
            return new TermQuery(new Term(field, text));
        } else {
            try {
                return new QueryParser(LUCENE_VERSION, null, new StandardAnalyzer(LUCENE_VERSION)).parse(field + ":" + text);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void visitOther(QueryTreeWalker<Context> walker, Tree tree, Context context) {
        walkThroughChildren(walker, tree, context);
    }

    private static class ForwardingContext extends Context {
        private final Context context;

        public ForwardingContext(Context context) {
            this.context = context;
            setFieldName(context.getFieldName());
            setOperator(context.getOperator());
        }

        @Override
        public void addSubQuery(Query query) {
            context.addSubQuery(query);
        }

        @Override
        public void addNegatedSubQuery(Query query) {
            context.addNegatedSubQuery(query);
        }
    }
}
