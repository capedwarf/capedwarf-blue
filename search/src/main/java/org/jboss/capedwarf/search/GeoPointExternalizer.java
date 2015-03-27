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

package org.jboss.capedwarf.search;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;

import com.google.appengine.api.search.GeoPoint;
import org.infinispan.commons.marshall.Externalizer;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GeoPointExternalizer implements Externalizer<GeoPoint>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final GeoPointExternalizer INSTANCE = new GeoPointExternalizer();

    public void writeObject(ObjectOutput output, GeoPoint gp) throws IOException {
        output.writeDouble(gp.getLatitude());
        output.writeDouble(gp.getLongitude());
    }

    public GeoPoint readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        return new GeoPoint(input.readDouble(), input.readDouble());
    }

    Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
