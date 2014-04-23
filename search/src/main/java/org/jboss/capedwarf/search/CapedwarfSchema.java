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

package org.jboss.capedwarf.search;

import java.util.Map;
import java.util.Set;

import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Schema;
import com.google.common.collect.SetMultimap;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.config.CacheName;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class CapedwarfSchema implements SchemaAdapter {
    private final String appId;
    private final String schemaName;

    CapedwarfSchema(String schemaName) {
        this.appId = AppIdFactory.getAppId();
        this.schemaName = schemaName;
    }

    public void addFields(SetMultimap<String, Field.FieldType> fields) {
        InfinispanUtils.fire(appId, CacheName.DIST, new AddFieldsTask(schemaName, fields), schemaName);
    }

    public void removeFields(Set<String> fieldNames) {
        InfinispanUtils.fire(appId, CacheName.DIST, new RemoveFieldsTask(schemaName, fieldNames), schemaName);
    }

    public Schema buildSchema() {
        final Schema.Builder builder = Schema.newBuilder();
        final SetMultimap<String, Field.FieldType> fields = InfinispanUtils.submit(appId, CacheName.DIST, new GetFieldsTask(schemaName), schemaName);
        if (fields != null && fields.size() > 0) {
            for (Map.Entry<String, Field.FieldType> entry : fields.entries()) {
                builder.addTypedField(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    public void deleteSchema() {
        InfinispanUtils.getCache(appId, CacheName.DIST).remove(schemaName);
    }
}
