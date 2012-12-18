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
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class QueueXmlParser {
    private static final String TOTAL_STORAGE_LIMIT_TAG = "total-storage-limit";
    private static final String QUEUEENTRIES_TAG = "queue-entries";
    private static final String QUEUE_TAG = "queue";
    private static final String NAME_TAG = "name";
    private static final String RATE_TAG = "rate";
    private static final String BUCKET_SIZE = "bucket-size";
    private static final String MAX_CONCURRENT_REQUESTS = "max-concurrent-requests";
    private static final String MODE_TAG = "mode";
    private static final String RETRY_PARAMETERS_TAG = "retry-parameters";
    private static final String TASK_RETRY_LIMIT_TAG = "task-retry-limit";
    private static final String TASK_AGE_LIMIT_TAG = "task-age-limit";
    private static final String MIN_BACKOFF_SECONDS_TAG = "min-backoff-seconds";
    private static final String MAX_BACKOFF_SECONDS_TAG = "max-backoff-seconds";
    private static final String MAX_DOUBLINGS_TAG = "max-doublings";
    private static final String TARGET_TAG = "target";
    private static final String ACL_TAG = "acl";
    private static final String USER_EMAIL_TAG = "user-email";
    private static final String WRITER_EMAIL_TAG = "writer-email";

    public static QueueXml parse(InputStream is)  {
        if (is == null) {
            return new QueueXml();
        }

        QueueXml queueXml = new QueueXml();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            if (QUEUEENTRIES_TAG.equals(doc.getDocumentElement().getTagName()) == false) {
                throw new QueueConfigException("queue.xml does not contain <queue-entries>");
            }
            NodeList list = doc.getDocumentElement().getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (QUEUE_TAG.equals(element.getTagName())) {
                        parseQueueTag(queueXml, element);
                    }
                }
            }
            return queueXml;
        } catch (ParserConfigurationException e) {
            throw new QueueConfigException("Could not parse WEB-INF/queue.xml", e);
        } catch (SAXException e) {
            throw new QueueConfigException("Could not parse WEB-INF/queue.xml", e);
        } catch (IOException e) {
            throw new QueueConfigException("Could not parse WEB-INF/queue.xml", e);
        }
    }

    private static void parseQueueTag(QueueXml queueXml, Element queue) {
        String name = getBody(getChildElement(queue, NAME_TAG));
        String mode = getBody(getChildElement(queue, MODE_TAG, true));
        queueXml.addQueue(name, (mode != null) ? QueueXml.Mode.valueOf(mode.toUpperCase()) : null);
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
            throw new QueueConfigException(parent.getTagName() + " does not contain <" + childName + ">");
        }
    }
}
