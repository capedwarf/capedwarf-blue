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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class DumpFileReader {

    private final ResultSet rset;
    private final Connection connection;

    public DumpFileReader(File sqliteFile) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load sqlite JDBC driver class", e);
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
            rset = connection.createStatement().executeQuery("SELECT * FROM result");

        } catch (SQLException e) {
            throw new RuntimeException("Error reading sqlite file " + sqliteFile.getAbsolutePath(), e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<byte[]> iterator() {
        return new ResultSetIterator(rset);
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
            return next() != null;
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
