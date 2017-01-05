/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.common.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ForwardingMap;

/**
 * This map only serializes keys and values that are serializable.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SerializableMap<K, V> extends ForwardingMap<K, V> implements Externalizable {
    private Map<K, V> delegate;

    public SerializableMap() {
        this(new ConcurrentHashMap<K, V>());
    }

    public SerializableMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    protected Map<K, V> delegate() {
        return delegate;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        Map<K, V> copy = new ConcurrentHashMap<>();
        for (Map.Entry<K, V> entry : delegate().entrySet()) {
            if ((entry.getKey() instanceof Serializable) && (entry.getValue() instanceof Serializable)) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        out.writeObject(copy);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        delegate = (Map<K, V>) in.readObject();
    }
}
