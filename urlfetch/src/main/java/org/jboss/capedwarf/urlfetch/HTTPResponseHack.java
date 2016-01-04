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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jboss.capedwarf.common.io.IOUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class HTTPResponseHack {
    static HTTPResponse response(HttpResponse response, URL finalUrl) throws Exception {
        int responseCode = response.getStatusLine().getStatusCode();

        InputStream is = response.getEntity().getContent();
        byte[] content = IOUtils.toBytes(is, true);

        List<HTTPHeader> headers = new ArrayList<>();
        for (Header h : response.getAllHeaders()) {
            headers.add(new HTTPHeader(h.getName(), h.getValue()));
        }

        return new HTTPResponse(responseCode, content, finalUrl, headers);
    }
}
