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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.capedwarf.common.io.Base64Utils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileUploader {

    public String getUploadUrl(URL url) throws URISyntaxException, IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url.toURI());
            HttpResponse response = httpClient.execute(get);
            return EntityUtils.toString(response.getEntity()).trim();
        }
    }

    public String uploadFile(String uri, String partName, String filename, String mimeType, byte[] contents) throws URISyntaxException, IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ByteArrayBody contentBody = new ByteArrayBody(contents, ContentType.create(mimeType), filename);
            builder.addPart(partName, contentBody);
            post.setEntity(builder.build());

            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * This method simulates a HTTP multipart form POST, where the user submits the form without actually selecting a file
     * to upload. Most browsers leave the "filename" part of the content-disposition header empty (they do not omit it
     * completely).
     */
    public String uploadWithoutFile(String uri, String partName) throws URISyntaxException, IOException {
        return uploadFile(uri, partName, "", "application/octet-stream", new byte[0]);
    }

    public String putFile(String uri, String partName, String filename, String mimeType, byte[] contents, String location) throws URISyntaxException, IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut put = new HttpPut(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ByteArrayBody contentBody = new ByteArrayBody(contents, ContentType.create(mimeType), filename);
            builder.addPart(partName, contentBody);
            put.setEntity(builder.build());

            put.addHeader("Authorization", "BASIC " + Base64Utils.encode("admin:admin".getBytes()));

            if (location != null) {
                put.addHeader("X-Location", location);
            }

            HttpResponse response = httpClient.execute(put);
            Header header = response.getFirstHeader("X-Location");
            return (header != null) ? header.getValue() : null;
        }
    }

    private static void addEntry(JarOutputStream jas, String name, byte[] content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jas.putNextEntry(entry);
        if (content != null) {
            jas.write(content);
        }
        jas.closeEntry();
    }

    public static void main(String[] args) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jas = new JarOutputStream(baos);

        addEntry(jas, "hello.txt", null);

        /*
        Properties properties = new Properties();
        properties.setProperty("groupId", "org.acme");
        properties.setProperty("artifactId", "acme-core");
        properties.setProperty("version", "1.0");
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        properties.store(b1, null);
        addEntry(jas, "META-INF/maven/org.acme/acme-core/1.0/pom.properties", b1.toByteArray());
        */

        jas.close();

        final FileUploader uploader = new FileUploader();

        String location = uploader.putFile("http://localhost:8181/maven/upload/acme-core-1.0.jar", "file", "acme-core-1.0.jar", "application/octet-stream", baos.toByteArray(), null);
        System.out.println("Location = " + location);

        location = uploader.putFile("http://localhost:8181/maven/upload/org.acme/acme-core/1.0/acme-core.jar", "file", "acme-core-1.0.jar", "application/octet-stream", baos.toByteArray(), location);
        System.out.println("Location = " + location);
    }
}
