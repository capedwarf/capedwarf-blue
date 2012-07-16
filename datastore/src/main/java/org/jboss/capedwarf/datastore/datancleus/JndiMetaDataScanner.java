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

package org.jboss.capedwarf.datastore.datancleus;

import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.datanucleus.metadata.MetaDataScanner;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.jboss.capedwarf.common.app.Application;

/**
 * Uses prepared Jandex based JNDI metadata scanner.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JndiMetaDataScanner implements MetaDataScanner {
    public Set<String> scanForPersistableClasses(PersistenceUnitMetaData pumd) {
        try {
            final Context context = new InitialContext();
            try {
                //noinspection unchecked
                return (Set<String>) context.lookup("java:jboss/capedwarf/persistence/entities/" + Application.getAppId());
            } finally {
                context.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
