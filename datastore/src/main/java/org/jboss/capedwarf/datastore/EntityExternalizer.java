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

package org.jboss.capedwarf.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.infinispan.marshall.Externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EntityExternalizer implements Externalizer<Entity> {

    public void writeObject(ObjectOutput out, Entity entity) throws IOException {
        out.writeUTF(KeyFactory.keyToString(entity.getKey()));
        out.writeInt(entity.getProperties().size());
        for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(convertPropertyValue(entry.getValue()));
        }
    }

    private Object convertPropertyValue(Object propertyValue) {
        if (propertyValue instanceof Integer) {
            Integer value = (Integer) propertyValue;
            return (long)value;
        } else if (propertyValue instanceof Short) {
            Short value = (Short) propertyValue;
            return (long)value;
        }
        return propertyValue;
    }

    public Entity readObject(ObjectInput in) throws IOException, ClassNotFoundException {
        Key key = KeyFactory.stringToKey(in.readUTF());
        Entity entity = new Entity(key);
        int numberOfProperties = in.readInt();
        for (int i=0; i<numberOfProperties; i++) {
            String propertyName = in.readUTF();
            Object propertyValue = in.readObject();
            entity.setProperty(propertyName, propertyValue);
        }
        return entity;
    }
}
