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

package org.jboss.capedwarf.endpoints;

import java.io.IOException;

import com.google.appengine.repackaged.org.codehaus.jackson.JsonParser;
import com.google.appengine.repackaged.org.codehaus.jackson.map.DeserializationContext;
import com.google.appengine.repackaged.org.codehaus.jackson.map.JsonDeserializer;
import org.jboss.capedwarf.shared.endpoints.Converters;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class EndpointsJsonDeserializer<T> extends JsonDeserializer<T> {
    private Class<?> start;
    private Class<?> end;

    protected EndpointsJsonDeserializer(Class<?> start) {
        this.start = start;
    }

    private synchronized Class<?> getEnd() {
        if (end == null) {
            end = Converters.getInstance().traverse(start);
        }
        return end;
    }

    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        Object initial = jsonParser.readValueAs(getEnd());
        return (T) Converters.getInstance().transformFrom(initial);
    }
}
