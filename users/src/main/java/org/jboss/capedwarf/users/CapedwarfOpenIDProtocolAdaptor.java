/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.users;

import org.jboss.capedwarf.appidentity.CapedwarfHttpServletRequestWrapper;
import org.picketlink.identity.federation.api.openid.OpenIDAttributeMap;
import org.picketlink.identity.federation.api.openid.OpenIDLifecycle;
import org.picketlink.identity.federation.api.openid.OpenIDLifecycleEvent;
import org.picketlink.identity.federation.api.openid.OpenIDProtocolAdapter;
import org.picketlink.identity.federation.api.openid.exceptions.OpenIDLifeCycleException;
import org.picketlink.identity.federation.api.openid.exceptions.OpenIDProtocolException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
class CapedwarfOpenIDProtocolAdaptor implements OpenIDProtocolAdapter, OpenIDLifecycle {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private String returnUrl;

    public CapedwarfOpenIDProtocolAdaptor(HttpServletRequest request, HttpServletResponse response, String returnUrl) {
        this.request = request;
        this.response = response;
        this.returnUrl = returnUrl;
    }

    public Object getAttributeValue(String name) {
        return request.getSession().getAttribute(name);
    }

    public OpenIDAttributeMap getAttributeMap() {
        return new OpenIDAttributeMap();
    }

    public String getReturnURL() {
        return returnUrl;
    }

    public void handle(OpenIDLifecycleEvent[] eventArr) throws OpenIDLifeCycleException {
        for (OpenIDLifecycleEvent ev : eventArr) {
            this.handle(ev);
        }
    }

    public void handle(OpenIDLifecycleEvent event) throws OpenIDLifeCycleException {
        if (event.getEventType() == OpenIDLifecycleEvent.TYPE.SUCCESS) {
            String email = request.getParameter("openid.ext1.value.email");
            request.getSession().setAttribute(
                    CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY,
                    new CapedwarfUserPrincipal(email));

            try {
                response.sendRedirect(request.getParameter(AuthServlet.DESTINATION_URL_PARAM));
            } catch (IOException e) {
                throw new OpenIDLifeCycleException(e);
            }
        } else if (event.getEventType() == OpenIDLifecycleEvent.TYPE.SESSION) {
            String attr = event.getAttributeName();
            Object attrVal = event.getAttributeValue();

            if (event.getOperation() == OpenIDLifecycleEvent.OP.ADD) {
                request.getSession().setAttribute(attr, attrVal);
            } else if (event.getOperation() == OpenIDLifecycleEvent.OP.REMOVE) {
                request.getSession().removeAttribute(attr);
            }
        }
    }

    public void sendToProvider(int version, String destinationURL, Map<String, String> paramMap) throws OpenIDProtocolException {
        if (version == 1) {
            sendWithRedirect(destinationURL);
        }
        sendWithPost(destinationURL, paramMap);
    }

    private void sendWithRedirect(String destinationURL) throws OpenIDProtocolException {
        try {
            response.sendRedirect(destinationURL);
        } catch (IOException e) {
            throw new OpenIDProtocolException(e);
        }
    }

    private void sendWithPost(String destinationURL, Map<String, String> paramMap) throws OpenIDProtocolException {
        // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
        try {
            response.setContentType("text/html");

            PrintWriter out = response.getWriter();
            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            out.println("<head>\n");
            out.println("    <title>OpenID HTML FORM Redirection</title>\n");
            out.println("</head>\n");
            out.println("<body onload=\"document.forms['openid-form-redirection'].submit();\">\n");
            out.println("    <form name=\"openid-form-redirection\" action=\"" + destinationURL + "\" method=\"post\" accept-charset=\"utf-8\">\n");

            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                out.println("        <input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>\n");
            }

            out.println("        <input type=\"hidden\" name=\"openid.ns.ax\" value=\"http://openid.net/srv/ax/1.0\"/>\n");
            out.println("        <input type=\"hidden\" name=\"openid.ax.mode\" value=\"fetch_request\"/>\n");
            out.println("        <input type=\"hidden\" name=\"openid.ax.type.email\" value=\"http://axschema.org/contact/email\"/>\n");
            out.println("        <input type=\"hidden\" name=\"openid.ax.required\" value=\"email\"/>\n");
            out.println("        <button type=\"submit\">Continue...</button>\n");
            out.println("    </form>\n");
            out.println("</body>\n");
            out.println("</html>");
        } catch (IOException e) {
            throw new OpenIDProtocolException(e);
        }
    }
}
