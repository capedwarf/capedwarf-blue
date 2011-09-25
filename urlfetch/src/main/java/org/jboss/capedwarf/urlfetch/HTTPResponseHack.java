/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.urlfetch;

import com.google.appengine.api.urlfetch.HTTPResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class HTTPResponseHack {

    private static final Constructor<HTTPResponse> ctor;
    private static final Method addHeader;
    private static final Method setContent;
    private static final Method setFinalUrl;

    static {
        try {
            Class<HTTPResponse> clazz = HTTPResponse.class;

            ctor = clazz.getConstructor(int.class);
            ctor.setAccessible(true);

            addHeader = clazz.getDeclaredMethod("addHeader", String.class, String.class);
            addHeader.setAccessible(true);

            setContent = clazz.getDeclaredMethod("setContent", byte[].class);
            setContent.setAccessible(true);

            setFinalUrl = clazz.getDeclaredMethod("setFinalUrl", URL.class);
            setFinalUrl.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    private HTTPResponse response;

    protected static void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }

    HTTPResponseHack(HttpResponse response) throws Exception {
        createResponse(response.getStatusLine().getStatusCode());
        for (Header h : response.getAllHeaders())
            addHeader(h.getName(), h.getValue());

        InputStream is = response.getEntity().getContent();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(is, baos);
            setContent(baos.toByteArray());
        } finally {
            is.close();
        }
    }

    HTTPResponse getResponse() {
        return response;
    }

    void createResponse(int responseCode) throws Exception {
        response = ctor.newInstance(responseCode);
    }

    void addHeader(String name, String value) throws Exception {
        addHeader.invoke(response, name, value);
    }

    void setContent(byte[] content) throws Exception {
        setContent.invoke(response, new Object[]{content});
    }

    void setFinalUrl(URL finalUrl) throws Exception {
        setFinalUrl.invoke(response, finalUrl);
    }
}