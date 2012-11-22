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

package org.jboss.test.capedwarf.prospectivesearch.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MatchResponseServlet extends HttpServlet {

    private static List<InvocationData> invocations = new ArrayList<InvocationData>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InvocationData invocationData = new InvocationData();
        invocations.add(invocationData);

        invocationData.key = request.getParameter("key");
        invocationData.topic = request.getParameter("topic");
        invocationData.resultsOffset = Integer.parseInt(request.getParameter("results_offset"));
        invocationData.resultsCount = Integer.parseInt(request.getParameter("results_count"));
        invocationData.subIds = request.getParameterValues("id");
        if (request.getParameter("document") != null) {
            invocationData.lastReceivedDocument = ProspectiveSearchServiceFactory.getProspectiveSearchService().getDocument(request);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public static int getInvocationCount() {
        return invocations.size();
    }

    public static boolean isInvoked() {
        return getInvocationCount() > 0;
    }

    public static List<InvocationData> getInvocations() {
        return invocations;
    }

    public static InvocationData getLastInvocationData() {
        return invocations.get(invocations.size()-1);
    }

    public static void clear() {
        invocations.clear();
    }

    public static List<String> getAllSubIds() {
        List<String> receivedSubIds = new ArrayList<String>();
        for (MatchResponseServlet.InvocationData invocationData : getInvocations()) {
            receivedSubIds.addAll(Arrays.asList(invocationData.getSubIds()));
        }
        return receivedSubIds;
    }

    public static class InvocationData {
        private Entity lastReceivedDocument;
        private int resultsOffset;
        private int resultsCount;
        private String[] subIds;
        private String key;
        private String topic;

        public Entity getDocument() {
            return lastReceivedDocument;
        }

        public int getResultsOffset() {
            return resultsOffset;
        }

        public int getResultsCount() {
            return resultsCount;
        }

        public String[] getSubIds() {
            return subIds;
        }

        public String getKey() {
            return key;
        }

        public String getTopic() {
            return topic;
        }
    }
}
