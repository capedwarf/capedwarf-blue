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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileUploader {

    public String getUploadUrl(URL url) throws URISyntaxException, IOException {
        HttpClient httpClient = new DefaultHttpClient();

        HttpGet get = new HttpGet(url.toURI());
        HttpResponse response = httpClient.execute(get);
        return EntityUtils.toString(response.getEntity()).trim();
    }

    public String uploadFile(String uri, String partName, String filename, String mimeType, byte[] contents) throws URISyntaxException, IOException {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost post = new HttpPost(uri);
        MultipartEntity entity = new MultipartEntity();
        ByteArrayBody contentBody = new ByteArrayBody(contents, mimeType, filename);
        entity.addPart(partName, contentBody);
        post.setEntity(entity);

        HttpResponse response = httpClient.execute(post);
        return EntityUtils.toString(response.getEntity());
    }

}
