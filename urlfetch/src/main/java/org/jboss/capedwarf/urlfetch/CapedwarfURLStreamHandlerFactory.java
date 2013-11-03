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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.collect.Sets;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.components.Key;
import org.jboss.capedwarf.shared.components.SimpleKey;
import org.jboss.capedwarf.shared.servlet.CapedwarfApiProxy;
import org.jboss.capedwarf.shared.url.URLHack;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapedwarfURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
    private static final CapedwarfURLStreamHandler HANDLER = new CapedwarfURLStreamHandler();

    private static final MethodInvocation<URLStreamHandler> getURLStreamHandler;
    private static final MethodInvocation<HttpURLConnection> openConnectionDirect;
    private static final MethodInvocation<HttpURLConnection> openConnectionWithProxy;

    static {
        getURLStreamHandler = ReflectionUtils.cacheMethod(URL.class, "getURLStreamHandler", String.class);
        openConnectionDirect = ReflectionUtils.cacheMethod(URLStreamHandler.class, "openConnection", URL.class);
        openConnectionWithProxy = ReflectionUtils.cacheMethod(URLStreamHandler.class, "openConnection", URL.class, Proxy.class);
    }

    private static final ThreadLocal<Set<String>> reentered = new ThreadLocal<Set<String>>() {
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    };

    private static Map<String, URLStreamHandler> defaultHandlers = new ConcurrentHashMap<String, URLStreamHandler>();

    public URLStreamHandler createURLStreamHandler(final String protocol) {
        if (PROTOCOLS.contains(protocol) == false)
            return null;

        Set<String> set = reentered.get();
        try {
            if (set.add(protocol)) {
                return URLHack.inLock(new Callable<URLStreamHandler>() {
                    public URLStreamHandler call() throws Exception {
                        // fetch defaults
                        defaultHandlers.put(protocol, getDefaultHandler(protocol));

                        return HANDLER;
                    }
                });
            } else {
                return null;
            }
        } finally {
            reentered.remove();
        }
    }

    private URLStreamHandler getDefaultHandler(String protocol) {
        URLStreamHandler handler = getURLStreamHandler.invoke(protocol);
        URLHack.removeHandlerNoLock(protocol);
        return handler;
    }

    private static boolean isStreamHandlerEnabled() {
        final String appId = CapedwarfApiProxy.getAppId();
        final Key<Compatibility> key = new SimpleKey<>(appId, Compatibility.class);
        final Compatibility compatibility = Compatibility.getInstance(key);
        return (compatibility.isEnabled(Compatibility.Feature.IGNORE_CAPEDWARF_URL_STREAM_HANDLER) == false);
    }

    private static class CapedwarfURLStreamHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL u, Proxy p, boolean useProxy) throws IOException {
            HttpURLConnection delegate;
            String protocol = u.getProtocol();
            URLStreamHandler handler = defaultHandlers.get(protocol);
            if (useProxy) {
                delegate = openConnectionWithProxy.invokeWithTarget(handler, u, p);
            } else {
                delegate = openConnectionDirect.invokeWithTarget(handler, u);
            }

            if (CapedwarfApiProxy.isCapedwarfApp() && isStreamHandlerEnabled()) {
                Class<? extends HttpURLConnection> exactType = (delegate instanceof HttpsURLConnection) ? HttpsURLConnection.class : HttpURLConnection.class;
                return CapedwarfURLConnectionProxyHandler.wrap(exactType, delegate);
            } else {
                return delegate;
            }
        }

        protected URLConnection openConnection(URL u) throws IOException {
            return openConnection(u, null, false);
        }

        protected URLConnection openConnection(URL u, Proxy p) throws IOException {
            return openConnection(u, p, true);
        }
    }
}
