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

package org.jboss.capedwarf.bytecode.endpoints;

import java.util.ArrayList;
import java.util.Collection;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiSerializationProperty;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.annotation.Annotation;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.capedwarf.bytecode.Annotator;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DtoAnnotator extends Annotator {
    public DtoAnnotator(CtClass clazz) {
        super(clazz);
    }

    public void addAnnotations() throws Exception {
        for (CtMethod method : getClazz().getDeclaredMethods()) {
            ApiSerializationProperty apiSP = (ApiSerializationProperty) method.getAnnotation(ApiSerializationProperty.class);
            if (apiSP != null) {
                convertApiSerializationProperty(method, apiSP);
            }
        }
    }

    private void convertApiSerializationProperty(CtMethod method, ApiSerializationProperty apiSP) {
        final Collection<Annotation> annotations = new ArrayList<>();

        if (apiSP.ignored() == AnnotationBoolean.TRUE) {
            annotations.add(createAnnotation(JsonIgnore.class));
            annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore.class));
        }

        final String name = apiSP.name();
        if (name.length() > 0) {
            annotations.add(createAnnotation(JsonProperty.class, memberValueOf(name)));
            annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty.class, memberValueOf(name)));
        }

        if (annotations.size() > 0) {
            addAnnotationsToMethod(method, annotations);
        }
    }
}
