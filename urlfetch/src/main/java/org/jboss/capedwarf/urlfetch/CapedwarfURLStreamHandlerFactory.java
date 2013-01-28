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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpParams;
import org.jboss.capedwarf.common.reflection.FieldInvocation;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.shared.servlet.CapedwarfApiProxy;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapedwarfURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
    private static final CapedwarfURLStreamHandler HANDLER = new CapedwarfURLStreamHandler();

    private static final FieldInvocation<Map> handlers;
    private static final MethodInvocation<URLStreamHandler> getURLStreamHandler;
    private static final MethodInvocation<URLConnection> openConnectionDirect;
    private static final MethodInvocation<URLConnection> openConnectionWithProxy;

    static {
        handlers = ReflectionUtils.cacheField(URL.class, "handlers");
        getURLStreamHandler = ReflectionUtils.cacheMethod(URL.class, "getURLStreamHandler", String.class);
        openConnectionDirect = ReflectionUtils.cacheMethod(URLStreamHandler.class, "openConnection", URL.class);
        openConnectionWithProxy = ReflectionUtils.cacheMethod(URLStreamHandler.class, "openConnection", URL.class, Proxy.class);
    }

    private static void removeProtocol(String protocol) {
        Map map = handlers.invoke();
        map.remove(protocol);
    }

    private static final ThreadLocal<Set<String>> reentered = new ThreadLocal<Set<String>>() {
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    };

    private static Map<String, URLStreamHandler> defaultHandlers = new ConcurrentHashMap<String, URLStreamHandler>();

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (PROTOCOLS.contains(protocol) == false)
            return null;

        Set<String> set = reentered.get();
        try {
            if (set.add(protocol)) {
                // fetch defaults
                defaultHandlers.put(protocol, getDefaultHandler(protocol));

                return HANDLER;
            } else {
                return null;
            }
        } finally {
            reentered.remove();
        }
    }

    private URLStreamHandler getDefaultHandler(String protocol) {
        URLStreamHandler handler = getURLStreamHandler.invoke(protocol);
        removeProtocol(protocol);
        return handler;
    }

    private static class CapedwarfURLStreamHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL u, Proxy p, boolean useProxy) throws IOException {
            if (CapedwarfApiProxy.isCapedwarfApp()) {
                return new CapedwarfURLConnection(u);
            } else {
                String protocol = u.getProtocol();
                URLStreamHandler handler = defaultHandlers.get(protocol);
                if (useProxy)
                    return openConnectionWithProxy.invoke(handler, u, p);
                else
                    return openConnectionDirect.invoke(handler, u);
            }
        }

        protected URLConnection openConnection(URL u) throws IOException {
            return openConnection(u, null, false);
        }

        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            return openConnection(u, p, true);
        }
    }

    private static class CapedwarfURLConnection extends URLConnection {
        private ByteArrayOutputStream baos;
        private HttpResponse response;
        private Map<String, List<String>> headers;
        private ListMultimap<String, String> properties = ArrayListMultimap.create();

        private CapedwarfURLConnection(URL url) {
            super(url);
        }

        private HttpResponse getResponse() throws IOException {
            try {
                return getResponseInternal();
            } catch (Exception e) {
                Throwable cause = e.getCause();
                throw (cause instanceof IOException) ? (IOException) cause : new IOException(cause);
            }
        }

        private synchronized HttpResponse getResponseInternal() {
            try {
                if (response == null) {
                    HttpClient client = CapedwarfURLFetchService.getClient();
                    HttpPost post = new HttpPost(getURL().toURI()); // always post?
                    if (baos != null) {
                        post.setEntity(new ByteArrayEntity(baos.toByteArray()));
                    }
                    if (properties.size() > 0) {
                        HttpParams params = post.getParams();
                        for (String key : properties.keySet()) {
                            params.setParameter(key, properties.get(key));
                        }
                    }
                    response = client.execute(post);
                }
                return response;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public void connect() throws IOException {
        }

        public void setRequestProperty(String key, String value) {
            properties.removeAll(key);
            addRequestProperty(key, value);
        }

        public void addRequestProperty(String key, String value) {
            properties.put(key, value);
        }

        public String getRequestProperty(String key) {
            List<String> list = properties.get(key);
            return (list != null && list.size() > 0) ? list.get(0) : null;
        }

        @SuppressWarnings("unchecked")
        public Map getRequestProperties() {
            return properties.asMap();
        }

        public synchronized OutputStream getOutputStream() throws IOException {
            if (baos == null) {
                baos = new ByteArrayOutputStream();
            }
            return baos;
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

        public synchronized Map<String, List<String>> getHeaderFields() {
            if (headers == null) {
                Map<String, List<String>> tmp = new LinkedHashMap<String, List<String>>();
                for (Header h : getResponseInternal().getAllHeaders()) {
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
        }

        public InputStream getInputStream() throws IOException {
            return getResponse().getEntity().getContent();
        }
    }
}
