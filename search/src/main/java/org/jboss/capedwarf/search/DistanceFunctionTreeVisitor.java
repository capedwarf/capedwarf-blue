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

import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.query.QueryLexer;
import com.google.appengine.api.search.query.QueryTreeVisitor;
import com.google.appengine.api.search.query.QueryTreeWalker;
import com.google.appengine.repackaged.org.antlr.runtime.tree.Tree;

/**
 *
 */
public class DistanceFunctionTreeVisitor implements QueryTreeVisitor<Context.DistanceFunction> {

    public void visitSequence(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitConjunction(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitDisjunction(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitNegation(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitRestriction(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitFuzzy(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitLiteral(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitLessThan(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitLessOrEqual(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitGreaterThan(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitGreaterOrEqual(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitEqual(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitContains(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitValue(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
    }

    public void visitOther(QueryTreeWalker<Context.DistanceFunction> walker, Tree tree, Context.DistanceFunction distanceFunction) {
        if (tree.getType() == QueryLexer.FUNCTION) {
            String functionName = tree.getChild(0).getText();
            assert functionName.equals("distance");

            Tree args = tree.getChild(1);

            Tree arg0 = args.getChild(0);
            Tree arg1 = args.getChild(1);

            String fieldName;
            GeoPoint geoPoint;
            if (arg0.getChild(0).getText().equals("geopoint")) {
                fieldName = getFieldName(arg1);
                geoPoint = getGeoPoint(arg0);
            } else {
                fieldName = getFieldName(arg0);
                geoPoint = getGeoPoint(arg1);
            }

            distanceFunction.setFieldName(fieldName);
            distanceFunction.setGeoPoint(geoPoint);
        }
    }

    private String getFieldName(Tree arg0) {
        return arg0.getChild(1).getText();
    }

    private GeoPoint getGeoPoint(Tree arg1) {
        GeoPoint geoPoint;
        double latitude = Double.valueOf(arg1.getChild(1).getChild(0).getChild(1).getText());
        double longitude = Double.valueOf(arg1.getChild(1).getChild(1).getChild(1).getText());

        geoPoint = new GeoPoint(latitude, longitude);
        return geoPoint;
    }
}
