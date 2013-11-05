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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfURLFetchService implements URLFetchService {
    private static TargetInvocation<Boolean> getFollowRedirects = ReflectionUtils.cacheInvocation(FetchOptions.class, "getFollowRedirects");

    public HTTPResponse fetch(URL url) throws IOException {
        return fetch(new HTTPRequest(url));
    }

    public HTTPResponse fetch(final HTTPRequest httpRequest) throws IOException {
        return fetch(toHttpUriRequest(httpRequest));
    }

    public Future<HTTPResponse> fetchAsync(URL url) {
        return fetchAsync(new HTTPRequest(url));
    }

    public Future<HTTPResponse> fetchAsync(final HTTPRequest httpRequest) {
        final HttpUriRequest request = toHttpUriRequest(httpRequest);

        final String appId = AppIdFactory.getAppId();
        final AppIdFactory appIdFactory = new AppIdFactory() {
            public String appId() {
                return appId;
            }
        };

        return ExecutorFactory.wrap(new Callable<HTTPResponse>() {
            public HTTPResponse call() throws Exception {
                AppIdFactory.setCurrentFactory(appIdFactory);
                try {
                    return fetch(request);
                } finally {
                    AppIdFactory.resetCurrentFactory();
                }
            }
        });
    }

    protected HttpUriRequest toHttpUriRequest(final HTTPRequest request) {
        final URI uri = toURI(request.getURL());

        final HttpUriRequest base;
        switch (request.getMethod()) {
            case POST:
                base = new HttpPost(uri);
                break;
            case GET:
                base = new HttpGet(uri);
                break;
            case PUT:
                base = new HttpPut(uri);
                break;
            case DELETE:
                base = new HttpDelete(uri);
                break;
            case HEAD:
                base = new HttpHead(uri);
                break;
            case PATCH:
                base = new HttpPatch(uri);
                break;
            default:
                throw new IllegalArgumentException("No such method supported: " + request.getMethod());
        }

        if (base instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase enclosing = (HttpEntityEnclosingRequestBase) base;
            byte[] payload = request.getPayload();
            if (payload != null && payload.length > 0) {
                enclosing.setEntity(new ByteArrayEntity(payload));
            }
        }

        FetchOptions options = request.getFetchOptions();
        if (options != null) {
            boolean followRedirects = getFollowRedirects.invokeUnchecked(options);
            base.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, followRedirects);

            // TODO -- other options
        }

        List<HTTPHeader> headers = request.getHeaders();
        if (headers != null && headers.size() > 0) {
            for (HTTPHeader header : headers) {
                base.addHeader(new BasicHeader(header.getName(), header.getValue()));
            }
        }

        return base;
    }

    protected HTTPResponse fetch(final HttpUriRequest request) throws IOException {
        // this one is OK, as it's needed in async invocation
        Compatibility.enable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        try {
            HttpClient client = ComponentRegistry.getInstance().getComponent(Keys.HTTP_CLIENT);
            HttpContext context = new BasicHttpContext();
            HttpResponse response = client.execute(request, context);
            HTTPResponseHack jhr = new HTTPResponseHack(response);

            String finalURLString = (String) context.getAttribute("final.url");
            URL finalURL;
            if (finalURLString != null) {
                if (finalURLString.contains(":/")) {
                    finalURL = new URL(finalURLString);
                } else {
                    finalURL = new URL("http", "localhost", 8080, finalURLString); // TODO
                }
            } else {
                finalURL = request.getURI().toURL();
            }
            jhr.setFinalUrl(finalURL);

            return jhr.getResponse();
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        }
    }

    protected URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
