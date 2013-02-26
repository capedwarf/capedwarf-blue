/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.test.capedwarf.blobstore.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UploadHandlerServlet extends HttpServlet {

    private static BlobKey lastUploadedBlobKey;
    private static BlobInfo lastUploadedBlobInfo;
    private static FileInfo lastUploadedFileInfo;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();

        PrintWriter out = response.getWriter();

        Map<String, List<BlobKey>> uploadMap = blobstore.getUploads(request);
        for (Map.Entry<String, List<BlobKey>> entry : uploadMap.entrySet()) {
            for (BlobKey blobKey : entry.getValue()) {
                out.println(entry.getKey() + ": " + blobKey);
                lastUploadedBlobKey = blobKey;
            }
        }

        Map<String, List<BlobInfo>> bInfos = blobstore.getBlobInfos(request);
        for (Map.Entry<String, List<BlobInfo>> entry : bInfos.entrySet()) {
            for (BlobInfo blobInfo : entry.getValue()) {
                out.println(entry.getKey() + ": " + blobInfo);
                lastUploadedBlobInfo = blobInfo;
            }
        }

        Map<String, List<FileInfo>> fInfos = blobstore.getFileInfos(request);
        for (Map.Entry<String, List<FileInfo>> entry : fInfos.entrySet()) {
            for (FileInfo fileInfo : entry.getValue()) {
                out.println(entry.getKey() + ": " + fileInfo);
                lastUploadedFileInfo = fileInfo;
            }
        }
    }

    public static BlobKey getLastUploadedBlobKey() {
        return lastUploadedBlobKey;
    }

    public static BlobInfo getLastUploadedBlobInfo() {
        return lastUploadedBlobInfo;
    }

    public static FileInfo getLastUploadedFileInfo() {
        return lastUploadedFileInfo;
    }
}
