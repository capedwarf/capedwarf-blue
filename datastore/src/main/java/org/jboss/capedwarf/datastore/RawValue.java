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

import org.jboss.capedwarf.datastore.query.Bridge;
import org.jboss.capedwarf.datastore.query.BridgeUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RawValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object value;

    public RawValue(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T asStrictType(Class<T> type) {
        try {
            return type.cast(asType(type));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Type mismatch");
        }
    }

    public Object asType(Class<?> type) {
        Bridge bridge = BridgeUtils.getBridge(type);
        return bridge.convertValue(value);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RawValue: " + getValue();
    }
}
