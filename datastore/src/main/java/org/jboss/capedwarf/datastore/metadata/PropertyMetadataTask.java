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

package org.jboss.capedwarf.datastore.metadata;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.datanucleus.store.types.sco.backed.Map;
import org.jboss.capedwarf.datastore.PropertyUtils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PropertyMetadataTask extends MetadataTask {
    private final Entity trigger;

    public PropertyMetadataTask(Entity trigger) {
        this.trigger = trigger;
    }

    protected void execute(DatastoreService ds) {
        List<Entity> entities = new ArrayList<Entity>();
        for (Map.Entry<String, Object> entry : trigger.getProperties().entrySet()) {
            if (PropertyUtils.isIndexedProperty(entry.getValue())) {
                Key key = Entities.createPropertyKey(trigger.getKind(), entry.getKey());
                entities.add(new Entity(key));
            }
        }
        ds.put(entities);
    }

    protected String getNamespace() {
        return trigger.getNamespace();
    }
}
