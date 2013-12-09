/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.sql;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.mysql.jdbc.GoogleDriver;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DriverCleanupHelper {
    private static final Logger log = Logger.getLogger(DriverCleanupHelper.class.getName());

    public static void cleanup(ClassLoader deploymentClassLoader) {
        final List<Driver> toUnregister = new ArrayList<>();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            Class<? extends Driver> clazz = driver.getClass();
            // make sure it's from our deployment
            if (deploymentClassLoader == clazz.getClassLoader() && (AppEngineDriver.class == clazz || GoogleDriver.class == clazz)) {
                toUnregister.add(driver);
            }
        }
        for (Driver driver : toUnregister) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                log.log(Level.WARNING, "Unable to deregister Driver automatically.", e);
            }
        }
    }
}
