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

package org.jboss.capedwarf.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DumpFileFacade {

    private Connection connection;
    private File sqliteFile;
    private PreparedStatement insertStatement;

    public DumpFileFacade(File sqliteFile) {
        this.sqliteFile = sqliteFile;
        loadJdbcDriver();
        openConnection();
        createResultsTableIfNotExists();
    }

    private void createResultsTableIfNotExists() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS result (\n" +
                "id BLOB primary key,\n" +
                "value BLOB not null,\n" +
                "sort_key BLOB);");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException("Error reading sqlite file " + sqliteFile.getAbsolutePath(), e);
        }
    }

    private void loadJdbcDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load sqlite JDBC driver class", e);
        }
    }

    public void add(byte[] id, byte[] entityPb, byte[] sortKey) {
        System.out.println("DumpFileFacade.add");
        try {
            if (insertStatement == null) {
                insertStatement = connection.prepareStatement("INSERT INTO result (id, value, sort_key) VALUES (?, ?, ?)");
            }
            insertStatement.setBytes(1, id);
            insertStatement.setBytes(2, entityPb);
            insertStatement.setBytes(3, sortKey);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<byte[]> iterator() {
        try {
            ResultSet rset = connection.createStatement().executeQuery("SELECT * FROM result");
            return new ResultSetIterator(rset);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static class ResultSetIterator implements Iterator<byte[]> {

        private byte[] next;
        private ResultSet rset;

        public ResultSetIterator(ResultSet rset) {
            this.rset = rset;
            readNextFromResultSet();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public byte[] next() {
            byte[] result = next;
            readNextFromResultSet();
            return result;
        }

        private void readNextFromResultSet() {
            try {
                if (rset.next()) {
                    next = rset.getBytes(2);
                } else {
                    next = null;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
