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

package org.jboss.capedwarf.common.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServletUtils {
    private static final String CONTENT_TYPE = "text/html";
    private static final String CHAR_ENCODING = "utf-8";

    public static boolean isFile(Part part) {
        return getFileName(part) != null;
    }

    public static String getFileName(Part part) {
        // TODO: surely, there is an existing servlet api method that does this. Googled it, but found nothing (!?)
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                String filename = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                if (filename.length() > 0) {
                	return filename;
                }
            }
        }
        return null;
    }

    /**
     * Set response headers, etc.
     *
     * @param response the http response
     */
    public static void handleResponse(HttpServletResponse response) {
        handleResponse(response, null, null);
    }

    /**
     * Set response headers, etc.
     *
     * @param response the http response
     * @param charEncoding the char encoding; can be null, default is used then
     * @param length the content length; only applied if not null
     */
    public static void handleResponse(HttpServletResponse response, String charEncoding, Integer length) {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(charEncoding != null ? charEncoding : CHAR_ENCODING);
        if (length != null) {
            response.setContentLength(length);
        }
    }

    public static String getRequestURIWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
