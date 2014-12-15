/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.servlet;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.SessionType;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CommonServletExtension implements ServletExtension {
    private static final Logger log = Logger.getLogger(CommonServletExtension.class.getName());

    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        ApplicationConfiguration configuration = ApplicationConfiguration.getInstance();
        AppEngineWebXml appEngineWebXml = configuration.getAppEngineWebXml();
        final SessionType sessionType = appEngineWebXml.getSessionType();
        switch (sessionType) {
            case APPENGINE:
            case STUB:
                deploymentInfo.setSessionManagerFactory(new DelegateSessionManagerFactory(appEngineWebXml));
                break;
            case WILDFLY:
                log.info("Using default WildFly http session configuration / handling.");
                break;
        }
    }
}
