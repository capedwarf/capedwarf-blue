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

package org.jboss.capedwarf.admin.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityTranslator;
import com.google.storage.onestore.v3.OnestoreEntity;
import org.jboss.capedwarf.datastore.ExposedDatastoreService;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RemoteApiServlet extends HttpServlet {
    private DatastoreService datastoreService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        datastoreService = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Compatibility.enable(Compatibility.Feature.IGNORE_LOGGING);
        try {
            final ExposedDatastoreService datastore = (ExposedDatastoreService) datastoreService;

            try (DataOutputStream out = new DataOutputStream(resp.getOutputStream())) {
                Iterator<Entity> entities = datastore.getAllEntitiesIterator();
                while (entities.hasNext()) {
                    Entity entity = entities.next();
                    OnestoreEntity.EntityProto entityProto = EntityTranslator.convertToPb(entity);
                    byte[] pbBytes = entityProto.toByteArray();
                    writeArray(out, entityProto.getKey().toByteArray());   // TODO: id
                    writeArray(out, pbBytes);
                    writeArray(out, new byte[0]);   // TODO: sort_key
                }
            }
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_LOGGING);
        }
    }

    private static void writeArray(DataOutputStream out, byte[] pbBytes) throws IOException {
        out.writeInt(pbBytes.length);
        out.write(pbBytes);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Compatibility.enable(Compatibility.Feature.IGNORE_LOGGING);
        try {
            try (DataInputStream in = new DataInputStream(req.getInputStream())) {
                while (true) {
                    int arrayLength;
                    try {
                        arrayLength = in.readInt();
                    } catch (EOFException e) {
                        break;
                    }
                    byte[] pbBytes = new byte[arrayLength];
                    in.readFully(pbBytes, 0, arrayLength);

                    Entity entity = EntityTranslator.createFromPbBytes(pbBytes);
                    datastoreService.put(entity);
                }
            }
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_LOGGING);
        }
    }
}
