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

package org.jboss.capedwarf.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.SerializabilityChecker;
import org.jboss.marshalling.SimpleClassResolver;
import org.jboss.marshalling.Unmarshaller;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MarshallingUtils {
    private static final MarshallerFactory factory;
    private static final SerializabilityChecker checker;

    public static final MarshallingType INTERNAL;
    public static final MarshallingType APP;

    static {
        factory = Marshalling.getMarshallerFactory("river", MarshallingUtils.class.getClassLoader());
        checker = new SerializabilityChecker() {
            public boolean isSerializable(Class<?> clazz) {
                return clazz != null;
            }
        };

        INTERNAL = MarshallingType.create(new SimpleClassResolver(MarshallingType.class.getClassLoader()));
        APP = create(new MarshallingConfiguration()); // default is TCCL resolver
    }

    static MarshallingType create(MarshallingConfiguration configuration) {
        try {
            if (configuration.getSerializabilityChecker() == null) {
                configuration.setSerializabilityChecker(checker);
            }

            Marshaller marshaller = factory.createMarshaller(configuration);
            Unmarshaller unmarshaller = factory.createUnmarshaller(configuration);

            return new MarshallingType(marshaller, unmarshaller);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static <T> T readObject(Unmarshaller unmarshaller, Class<T> clazz, byte[] bytes) {
        try {
            unmarshaller.start(Marshalling.createByteInput(new ByteArrayInputStream(bytes)));
            try {
                return unmarshaller.readObject(clazz);
            } finally {
                unmarshaller.finish();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static byte[] writeObject(Marshaller marshaller, Object object) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.start(Marshalling.createByteOutput(baos));
            try {
                marshaller.writeObject(object);
            } finally {
                marshaller.finish();
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
