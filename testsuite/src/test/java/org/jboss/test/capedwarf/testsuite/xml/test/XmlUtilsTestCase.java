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

package org.jboss.test.capedwarf.testsuite.xml.test;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.jboss.capedwarf.common.xml.XmlUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class XmlUtilsTestCase {

    private Element docElement;

    @Before
    public void setUp() throws Exception {
        String xml = "<doc>" +
                "<child>foo</child>" +
                "<child>bar</child>" +
                "<other>baz</other>" +
                "</doc>";
        Document doc = XmlUtils.parseXml(new ByteArrayInputStream(xml.getBytes()));
        docElement = doc.getDocumentElement();
    }

    @Test
    public void testParseXml() throws Exception {
        assertEquals("doc", docElement.getTagName());
    }

    @Test
    public void getChildElement_ReturnsFirstChildWithGivenName() throws Exception {
        Element child = XmlUtils.getChildElement(docElement, "child");
        assertEquals("child", child.getTagName());
        assertEquals("foo", child.getChildNodes().item(0).getNodeValue());
    }

    @Test
    public void getBody_ReturnsBodyOfGivenElement() throws Exception {
        Element child = (Element) docElement.getElementsByTagName("child").item(0);
        Assert.assertEquals("foo", XmlUtils.getBody(child));
    }

    @Test
    public void getChildElementBody_ReturnsBodyOfFirstChildWithGivenName() throws Exception {
        Assert.assertEquals("foo", XmlUtils.getChildElementBody(docElement, "child"));
    }

    @Test
    public void getChildren_ReturnsChildrenWithGivenName() throws Exception {
        List<Element> children = XmlUtils.getChildren(docElement, "child");
        assertEquals(2, children.size());
        Assert.assertEquals("foo", XmlUtils.getBody(children.get(0)));
        Assert.assertEquals("bar", XmlUtils.getBody(children.get(1)));
    }

}
