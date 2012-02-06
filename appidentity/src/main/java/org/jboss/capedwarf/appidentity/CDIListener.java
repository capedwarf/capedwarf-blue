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

package org.jboss.capedwarf.appidentity;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CDIListener implements ServletContextListener {

    private static Logger log = Logger.getLogger(CDIListener.class.getName());
    /**
     * The impl detail
     */
    private static final String BM_KEY = "org.jboss.weld.environment.servlet" + "." + BeanManager.class.getName();
    private static final String STANDARD_BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";

    protected BeanManager getBeanManager() {
        Context nc = null;
        try {
            nc = new InitialContext();
            return (BeanManager) nc.lookup(STANDARD_BEAN_MANAGER_JNDI_NAME);
        } catch (Exception e) {
            log.warning("Cannot find BeanManager: " + e);
            return null;
        } finally {
            if (nc != null) {
                try {
                    nc.close();
                } catch (NamingException ignored) {
                }
            }
        }
    }

    public void contextInitialized(ServletContextEvent sce) {
        final BeanManager manager = getBeanManager();
        if (manager != null)
            sce.getServletContext().setAttribute(BM_KEY, manager);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(BM_KEY);
    }
}
