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
public class TestContext implements Cloneable {
    private static final String ROOT = "ROOT.war";

    protected static final TestContext DEFAULT = asDefault();

    private String archiveName = "capedwarf-tests.war";

    private String webXmlContent = "<web/>";
    private String webXmlFile;

    private String appEngineWebXmlFile;

    private boolean contextRoot;

    private String compatibilityProperties;
    private Properties properties = new Properties();

    private boolean callbacks;

    private TestContext() {
    }

    private TestContext(String archiveName) {
        this.archiveName = archiveName;
    }

    private static TestContext asDefault(TestContext context) {
        return context.setIgnoreLogging(true).setDisableBlackList(true);
    }

    public static TestContext withName(String name) {
        return asDefault(new TestContext(name));
    }

    public static TestContext withLogging() {
        return new TestContext().setDisableBlackList(true);
    }

    public static TestContext withBlackList() {
        return new TestContext().setIgnoreLogging(true);
    }

    public static TestContext asDefault() {
        return asDefault(new TestContext());
    }

    public static TestContext asRoot() {
        return asDefault(new TestContext(ROOT)).setContextRoot(true);
    }

    public String getArchiveName() {
        return archiveName;
    }

    public TestContext setArchiveName(String archiveName) {
        this.archiveName = archiveName;
        return this;
    }

    public String getWebXmlContent() {
        return webXmlContent;
    }

    public TestContext setWebXmlContent(String webXmlContent) {
        this.webXmlContent = webXmlContent;
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

    public boolean isContextRoot() {
        return contextRoot;
    }

    public TestContext setContextRoot(boolean contextRoot) {
        this.contextRoot = contextRoot;
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

    public boolean hasCallbacks() {
        return callbacks;
    }

    public TestContext setCallbacks(boolean callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    public TestContext setIgnoreLogging(boolean ignoreLogging) {
        properties.put("ignore.logging", String.valueOf(ignoreLogging));
        return this;
    }

    public TestContext setDisableBlackList(boolean disableBlackList) {
        properties.put("disable.blacklist", String.valueOf(disableBlackList));
        return this;
    }
}
