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

package org.jboss.test.capedwarf.cluster.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.EntityTranslator;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author Matej Lazar
 */
public class ProspectiveSearchMatchResponseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InvocationData invocationData = new InvocationData();

        invocationData.key = request.getParameter("key");
        invocationData.topic = request.getParameter("topic");
        invocationData.resultsOffset = Long.parseLong(request.getParameter("results_offset"));
        invocationData.resultsCount = Long.parseLong(request.getParameter("results_count"));
        invocationData.subIds = request.getParameterValues("id");
//        if (request.getParameter("document") != null) {
//            invocationData.lastReceivedDocument = ProspectiveSearchServiceFactory.getProspectiveSearchService().getDocument(request);
//        }
        storeInvocationData(invocationData, request.getParameter("document"));
    }

    private void storeInvocationData(InvocationData invocationData, String reqDocument) {
        Key key = createEntityKey();
        Entity entity = new Entity(key);
        entity.setProperty("key", invocationData.key);
        entity.setProperty("topic", invocationData.topic);
        entity.setProperty("resultsOffset", invocationData.resultsOffset);
        entity.setProperty("resultsCount", invocationData.resultsCount);
        String subIdStr = "";
        subIdStr = subIdsArrToStr(invocationData.subIds, subIdStr);
        entity.setProperty("subIds", subIdStr);

        entity.setProperty("document", reqDocument);

        DatastoreService service = DatastoreServiceFactory.getDatastoreService();
        service.put(entity);
    }

    private String subIdsArrToStr(String[] subIds, String subIdStr) {
        for (String subId : subIds) {
            subIdStr += "~" + subId;
        }
        return subIdStr.substring(2);
    }

    private static String[] subIdsStrToArr(String subIdStr) {
        return subIdStr.split("~");
    }

    private static Key createEntityKey() {
        return KeyFactory.createKey("pst", 1);
    }

    public static InvocationData getInvocationData(DatastoreService service) throws EntityNotFoundException, Base64DecoderException {
        InvocationData invocationData = new InvocationData();

        Entity entity = service.get(createEntityKey());
        invocationData.key = (String) entity.getProperty("key");
        invocationData.topic = (String) entity.getProperty("topic");
        invocationData.resultsOffset = (Long) entity.getProperty("resultsOffset");
        invocationData.resultsCount = (Long) entity.getProperty("resultsCount");
        invocationData.subIds = subIdsStrToArr((String) entity.getProperty("subIds"));
        invocationData.lastReceivedDocument = decodeDocument((String) entity.getProperty("document"));

        return invocationData;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    private static Entity decodeDocument(String encodedDocument) throws Base64DecoderException {
        return EntityTranslator.createFromPbBytes(Base64.decodeWebSafe(encodedDocument));
    }

    public static class InvocationData {
        private Entity lastReceivedDocument;
        private Long resultsOffset;
        private Long resultsCount;
        private String[] subIds;
        private String key;
        private String topic;

        public Entity getDocument() {
            return lastReceivedDocument;
        }

        public Long getResultsOffset() {
            return resultsOffset;
        }

        public Long getResultsCount() {
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
