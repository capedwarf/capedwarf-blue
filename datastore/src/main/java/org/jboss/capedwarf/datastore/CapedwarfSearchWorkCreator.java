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

package org.jboss.capedwarf.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.infinispan.query.impl.DefaultSearchWorkCreator;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.IndexesXml;
import org.jboss.capedwarf.shared.datastore.DatastoreConstants;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class CapedwarfSearchWorkCreator extends DefaultSearchWorkCreator<Object> {

    @Override
    public Collection<Work> createPerEntityWorks(Object value, Serializable id, WorkType workType) {
        if (id == null) {
            return super.createPerEntityWorks(value, id, workType);
        }

        Entity entity = (Entity) value;
        List<Work> works = new ArrayList<>(super.createPerEntityWorks(value, id, workType));
        for (IndexesXml.Index index : ApplicationConfiguration.getInstance().getIndexesXml().getIndexes().values()) {
            if (index.getKind().equals(entity.getKind())) {
                int i = 0;
                for (Entity explodedEntity : explodeEntity(entity, index)) {
                    String explodedId = id + DatastoreConstants.SEPARATOR + i++ + DatastoreConstants.SEPARATOR + index.getName();
                    works.add(new Work(explodedEntity, explodedId, workType));
                }
            }
        }
        return works;
    }

    private List<Entity> explodeEntity(Entity entity, IndexesXml.Index index) {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        for (IndexesXml.Property property : index.getProperties()) {
            String propertyName = property.getName();

            if (!entity.hasProperty(propertyName)) {
                return Collections.emptyList();
            }

            Object propertyValue = entity.getProperty(propertyName);
            if (propertyValue instanceof Collection) {
                Collection<?> values = (Collection<?>) propertyValue;
                Set<?> distinctValues = new HashSet<>(values);
                List<Entity> newEntities = new ArrayList<>();
                for (Object value : distinctValues) {
                    for (Entity e : entities) {
                        Entity newE = new Entity(e.getKey());
                        newE.setPropertiesFrom(e);
                        newE.setProperty(propertyName, value);
                        newEntities.add(newE);
                    }
                }
                entities = newEntities;
            }
        }
        return entities;
    }
}
