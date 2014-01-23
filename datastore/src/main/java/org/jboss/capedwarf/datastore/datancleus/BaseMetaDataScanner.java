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

import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Key;
import org.jboss.capedwarf.shared.components.SetKey;
import org.jboss.capedwarf.shared.components.Slot;

/**
 * Uses prepared Jandex based metadata scanner.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BaseMetaDataScanner extends AbstractMetaDataScanner {
    private Set<String> entities;

    public BaseMetaDataScanner() {
    }

    public BaseMetaDataScanner(Set<String> entities) {
        this.entities = entities;
    }

    public synchronized Set<String> scanForPersistableClasses(PersistenceUnitMetaData pumd) {
        if (entities == null) {
            Key<Set<String>> key = new SetKey<String>(Slot.METADATA_SCANNER);
            entities = ComponentRegistry.getInstance().getComponent(key);
        }
        return entities;
    }
}
