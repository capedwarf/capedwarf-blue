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

package org.jboss.test.capedwarf.common.test;

import java.util.Properties;

/**
 * Test context.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestContext {
    protected static TestContext DEFAULT = new TestContext();

    private String archiveName = "capedwarf-tests.war";

    private String webXml = "<web/>";
    private String webXmlFile;

    private String appEngineWebXmlFile;

    private String compatibilityProperties;
    private Properties properties = new Properties();

    public TestContext() {
    }

    public TestContext(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public TestContext setArchiveName(String archiveName) {
        this.archiveName = archiveName;
        return this;
    }

    public String getWebXml() {
        return webXml;
    }

    public TestContext setWebXml(String webXml) {
        this.webXml = webXml;
        return this;
    }

    public String getWebXmlFile() {
        return webXmlFile;
    }

    public TestContext setWebXmlFile(String webXmlFile) {
        this.webXmlFile = webXmlFile;
        return this;
    }

    public String getAppEngineWebXmlFile() {
        return appEngineWebXmlFile;
    }

    public TestContext setAppEngineWebXmlFile(String appEngineWebXmlFile) {
        this.appEngineWebXmlFile = appEngineWebXmlFile;
        return this;
    }

    public String getCompatibilityProperties() {
        return compatibilityProperties;
    }

    public TestContext setCompatibilityProperties(String compatibilityProperties) {
        this.compatibilityProperties = compatibilityProperties;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public TestContext setIgnoreLogging(boolean ignoreLogging) {
        properties.put("ignore.logging", String.valueOf(ignoreLogging));
        return this;
    }
}
