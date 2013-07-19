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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.SimpleKey;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SqlUtils {
    public static DataSource getDataSource() {
        ComponentRegistry registry = ComponentRegistry.getInstance();
        DataSource ds = new JndiDataSource();
        DataSource previous = registry.putIfAbsent(new SimpleKey<DataSource>(EnvAppIdFactory.INSTANCE, DataSource.class), ds);
        return (previous != null ? previous : ds);
    }

    @SuppressWarnings("UnusedParameters")
    public static Connection connect(String url, Properties info) throws SQLException {
        String username = getProperty(info, "username");
        String password = getProperty(info, "password");
        return getDataSource().getConnection(username, password);
    }

    private static String getProperty(Properties properties, String key) {
        return (properties != null) ? properties.getProperty(key) : null;
    }
}
