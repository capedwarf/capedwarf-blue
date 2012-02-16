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

package org.jboss.test.capedwarf.tasks.support;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PrintServlet extends HttpServlet {
    private Logger log = Logger.getLogger(PrintServlet.class.getName());

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        log.info("Ping - " + req);
        
        final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity entity = new Entity("Qwert");
            entity.setProperty("xyz", 123);
            Key key = ds.put(entity);

            entity = ds.get(key);
            System.out.println(entity);

            FileService fs = FileServiceFactory.getFileService();
            AppEngineFile file = fs.createNewBlobFile("qwertfile");
            FileWriteChannel fwc = fs.openWriteChannel(file, false);
            fwc.write(ByteBuffer.wrap("qwert".getBytes()));
            fwc.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
