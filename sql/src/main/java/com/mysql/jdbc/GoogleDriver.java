package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.capedwarf.sql.SqlUtils;

/**
 * This is a *hack* to make GoogleDriver usage transparent in CapeDwarf.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GoogleDriver implements Driver {
    private static final Logger LOG = Logger.getLogger(GoogleDriver.class.getName());

    static {
        registerDriver();
    }

    private static void registerDriver() {
        try {
            DriverManager.registerDriver(new GoogleDriver());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Unable to register GoogleDriver automatically.", e);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        return SqlUtils.connect(url, info);
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.contains("mysql");
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0]; // TODO
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(GoogleDriver.class.getName()).getParent();
    }
}
