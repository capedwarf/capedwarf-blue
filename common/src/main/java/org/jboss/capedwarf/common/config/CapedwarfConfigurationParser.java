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

package org.jboss.capedwarf.common.config;

import org.jboss.capedwarf.common.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfConfigurationParser {

    public static CapedwarfConfiguration parse(InputStream inputStream) throws IOException {
        try {
            return tryParse(inputStream);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static CapedwarfConfiguration tryParse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        Document doc = XmlUtils.parseXml(inputStream);
        Element documentElement = doc.getDocumentElement();

        CapedwarfConfiguration config = new CapedwarfConfiguration();

        for (Element adminElem : XmlUtils.getChildren(documentElement, "admin")) {
            config.addAdmin(XmlUtils.getBody(adminElem));
        }
        return config;
    }


}
