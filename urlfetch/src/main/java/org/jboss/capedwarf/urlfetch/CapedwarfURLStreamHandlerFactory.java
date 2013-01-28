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

package org.jboss.capedwarf.urlfetch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.collect.Sets;
import org.jboss.capedwarf.shared.servlet.CapedwarfApiProxy;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapedwarfURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
    private static final CapedwarfURLStreamHandler HANDLER = new CapedwarfURLStreamHandler();

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (PROTOCOLS.contains(protocol) == false)
            return null;

        if (CapedwarfApiProxy.isCapedwarfApp())
            return null;

        return HANDLER;
    }

    private static class CapedwarfURLStreamHandler extends URLStreamHandler {

        protected URLConnection openConnection(URL u) throws IOException {
            return new CapedwarfURLConnection(u);
        }

        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            return openConnection(u);
        }
    }

    private static class CapedwarfURLConnection extends URLConnection {
        private volatile HTTPResponse response;
        private volatile Map<String, List<String>> headers;

        private CapedwarfURLConnection(URL url) {
            super(url);
        }

        private HTTPResponse getResponse() throws IOException {
            if (response == null) {
                URLFetchService service = URLFetchServiceFactory.getURLFetchService();
                response = service.fetch(getURL());
            }
            return response;
        }

        public void connect() throws IOException {
        }

        public String getHeaderField(String name) {
            if (name == null)
                return null;

            List<String> fields = getHeaderFields().get(name);
            return (fields != null && fields.size() > 0) ? fields.get(0) : null;
        }

        public String getHeaderField(int n) {
            return getHeaderField(getHeaderFieldKey(n));
        }

        public String getHeaderFieldKey(int n) {
            List<String> keys = new ArrayList<String>(getHeaderFields().keySet());
            return (keys.size() > n) ? keys.get(n) : null;
        }

        public Map<String, List<String>> getHeaderFields() {
            try {
                if (headers == null) {
                    Map<String, List<String>> tmp = new LinkedHashMap<String, List<String>>();
                    for (HTTPHeader h : getResponse().getHeaders()) {
                        String name = h.getName();
                        List<String> list = tmp.get(name);
                        if (list == null) {
                            list = new ArrayList<String>();
                            tmp.put(name, list);
                        }
                        list.add(h.getValue());
                    }
                    headers = tmp;
                }
                return headers;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(getResponse().getContent());
        }
    }
}
