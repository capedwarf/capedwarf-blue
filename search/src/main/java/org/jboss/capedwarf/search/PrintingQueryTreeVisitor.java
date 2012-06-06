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

import com.google.appengine.api.search.query.QueryTreeVisitor;
import com.google.appengine.api.search.query.QueryTreeWalker;
import com.google.appengine.repackaged.org.antlr.runtime.tree.Tree;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class PrintingQueryTreeVisitor implements QueryTreeVisitor<Integer> {
    public void visitSequence(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("sequence", walker, tree, level);
    }

    public void visitConjunction(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("conjunction", walker, tree, level);
    }

    public void visitDisjunction(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("disjunction", walker, tree, level);
    }

    public void visitNegation(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("negation", walker, tree, level);
    }

    public void visitRestriction(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("restriction", walker, tree, level);
    }

    public void visitFuzzy(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("fuzzy", walker, tree, level);
    }

    public void visitLiteral(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("literal", walker, tree, level);
    }

    public void visitLessThan(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("<", walker, tree, level);
    }

    public void visitLessOrEqual(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("<=", walker, tree, level);
    }

    public void visitGreaterThan(QueryTreeWalker walker, Tree tree, Integer level) {
        visit(">", walker, tree, level);
    }

    public void visitGreaterOrEqual(QueryTreeWalker walker, Tree tree, Integer level) {
        visit(">=", walker, tree, level);
    }

    public void visitEqual(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("=", walker, tree, level);
    }

    public void visitContains(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("contains", walker, tree, level);
    }

    public void visitValue(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("value", walker, tree, level);
    }

    public void visitOther(QueryTreeWalker walker, Tree tree, Integer level) {
        visit("other", walker, tree, level);
    }

    private void visit(String type, QueryTreeWalker walker, Tree tree, Integer level) {
        print(indent(level) + type + " " + tree.getText() + " (" + tree + ")");
        for (int i=0; i<tree.getChildCount(); i++) {
            Tree child = tree.getChild(i);
            walker.walk(child, level+1);
        }
    }

    protected void print(String text) {
        System.out.println(text);
    }

    private String indent(Integer level) {
        StringBuilder sbuf = new StringBuilder();
        for (int i=0; i<level; i++) {
            sbuf.append("  ");
        }
        return sbuf.toString();
    }
}
