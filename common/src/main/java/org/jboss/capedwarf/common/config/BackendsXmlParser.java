/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BackendsXmlParser {
    private static final String BACKENDS_TAG = "backends";
    private static final String BACKEND_TAG = "backend";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String CLASS_TAG = "class";
    private static final String MAX_CONCURRENT_REQUESTS_TAG = "max-concurrent-requests";
    private static final String OPTIONS_TAG = "options";
    private static final String DYNAMIC_TAG = "dynamic";
    private static final String FAIL_FAST_TAG = "fail-fast";
    private static final String PUBLIC_TAG = "public";

    public static Backends parse(InputStream is)  {
        if (is == null) {
            return new Backends();
        }

        Backends backends = new Backends();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            if (BACKENDS_TAG.equals(doc.getDocumentElement().getTagName()) == false) {
                throw new BackendsConfigException("backends.xml does not contain <backends>");
            }

            NodeList list = doc.getDocumentElement().getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (BACKEND_TAG.equals(element.getTagName())) {
                        parseBackendTag(backends, element);
                    }
                }
            }
            return backends;
        } catch (ParserConfigurationException e) {
            throw new BackendsConfigException("Could not parse WEB-INF/backends.xml", e);
        } catch (SAXException e) {
            throw new BackendsConfigException("Could not parse WEB-INF/backends.xml", e);
        } catch (IOException e) {
            throw new BackendsConfigException("Could not parse WEB-INF/backends.xml", e);
        }
    }

    private static void parseBackendTag(Backends backends, Element backend) {
        Backends.Backend bb = new Backends.Backend();
        String name = backend.getAttribute(NAME_ATTRIBUTE);
        if (name == null) {
            throw new BackendsConfigException("Null backend name!");
        }
        bb.setName(name);
        String mcr = getBody(getChildElement(backend, MAX_CONCURRENT_REQUESTS_TAG, true));
        int maxConcurrentRequests = (mcr != null) ? Integer.parseInt(mcr) : 0;
        bb.setMaxConcurrentRequests(maxConcurrentRequests);
        backends.addBackend(bb);
    }

    private static String getBody(Element element) {
        return (element != null) ? element.getTextContent() : null;
    }

    private static Element getChildElement(Element parent, String childName) {
        return getChildElement(parent, childName, false);
    }

    private static Element getChildElement(Element parent, String childName, boolean allowNull) {
        NodeList children = parent.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                Element element = (Element) child;
                if (childName.equals(element.getTagName())) {
                    return element;
                }
            }
        }
        if (allowNull) {
            return null;
        } else {
            throw new BackendsConfigException(parent.getTagName() + " does not contain <" + childName + ">");
        }
    }
}
