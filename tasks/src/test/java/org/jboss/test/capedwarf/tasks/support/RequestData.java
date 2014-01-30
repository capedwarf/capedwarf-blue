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

package org.jboss.test.capedwarf.tasks.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jboss.capedwarf.common.io.IOUtils;


/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class RequestData {
    private byte[] body;
    private Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public RequestData(HttpServletRequest req) throws IOException {
        storeHeaders(req);
        storeBody(req);
    }

    private void storeHeaders(HttpServletRequest req) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, req.getHeader(header));
        }
    }

    private void storeBody(HttpServletRequest req) throws IOException {
        try (ServletInputStream in = req.getInputStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyStream(in, baos);
            body = baos.toByteArray();
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public byte[] getBody() {
        return body;
    }
}
