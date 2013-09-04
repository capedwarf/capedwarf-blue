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

import com.google.appengine.api.search.Field;
import com.google.common.collect.SetMultimap;
import org.infinispan.AdvancedCache;
import org.jboss.capedwarf.common.infinispan.BaseTxTask;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractFieldsTask<R> extends BaseTxTask<String, SetMultimap<String, Field.FieldType>, R> {
    protected final String schemaName;

    protected AbstractFieldsTask(String schemaName) {
        this.schemaName = schemaName;
    }

    protected R callInTx() throws Exception {
        final AdvancedCache<String, SetMultimap<String, Field.FieldType>> ac = getCache().getAdvancedCache();

        if (ac.lock(schemaName) == false)
            throw new IllegalArgumentException("Cannot get a lock on schema for " + schemaName);

        final SetMultimap<String, Field.FieldType> map = ac.get(schemaName);
        return execute(map);
    }

    protected abstract R execute(SetMultimap<String, Field.FieldType> map);
}
