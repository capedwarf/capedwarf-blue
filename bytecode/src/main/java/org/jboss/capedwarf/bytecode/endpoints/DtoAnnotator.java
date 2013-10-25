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
import java.util.Random;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonDeserialize;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.capedwarf.bytecode.Annotator;
import org.jboss.capedwarf.endpoints.EndpointsJsonDeserializer;
import org.jboss.capedwarf.endpoints.EndpointsJsonSerializer;
import org.jboss.capedwarf.shared.endpoints.Converters;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DtoAnnotator extends Annotator {
    public DtoAnnotator(CtClass clazz) {
        super(clazz);
    }

    public void addAnnotations() throws Exception {
        final Converters converters = Converters.getInstance(getClassLoader());

        final boolean isResultType = converters.isResultType(getClazz());
        if (isResultType == false) {
            return;
        }

        for (CtMethod method : getClazz().getDeclaredMethods()) {
            boolean ignored = false;
            Collection<Annotation> annotations = new ArrayList<>();

            ApiResourceProperty apiRP = (ApiResourceProperty) method.getAnnotation(ApiResourceProperty.class);
            if (apiRP != null) {
                ignored = convertApiResourceProperty(apiRP, annotations);
            }

            if (ignored == false) {
                final CtClass returnType = method.getReturnType();
                if (converters.hasConverter(returnType)) {
                    addConverters(returnType, annotations);
                }
            }

            if (annotations.size() > 0) {
                addAnnotationsToMethod(method, annotations);
            }
        }
    }

    private boolean convertApiResourceProperty(ApiResourceProperty apiRP, Collection<Annotation> annotations) {
        boolean ignored = false;

        if (apiRP.ignored() == AnnotationBoolean.TRUE) {
            ignored = true;

            annotations.add(createAnnotation(JsonIgnore.class));
            annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore.class));
        }

        final String name = apiRP.name();
        if (name.length() > 0) {
            StringMemberValue value = memberValueOf(name);
            annotations.add(createAnnotation(JsonProperty.class, value));
            annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty.class, value));
        }

        return ignored;
    }

    private void addConverters(CtClass returnType, Collection<Annotation> annotations) {
        ClassMemberValue using1 = createClassMemberValue(EndpointsJsonSerializer.class);
        annotations.add(createAnnotation(JsonSerialize.class, "using", using1));
        annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonSerialize.class, "using", using1));

        ClassMemberValue using2 = createClassMemberValue(generateDeserializer(returnType));
        annotations.add(createAnnotation(JsonDeserialize.class, "using", using2));
        annotations.add(createAnnotation(com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonDeserialize.class, "using", using2));
    }

    protected String generateDeserializer(CtClass clazz) {
        try {
            return generateSimpleSub(EndpointsJsonDeserializer.class.getName(), clazz);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected static String getSimpleName(String className) {
        int p = className.lastIndexOf(".");
        return (p > 0) ? className.substring(p + 1) : className;
    }

    protected static String toDesc(String className) {
        return "L" + className.replace('.', '/');
    }

    protected String generateSimpleSub(String superClass, CtClass ctorParamClass) throws Exception {
        int p = superClass.lastIndexOf(".");
        String prefix = (p > 0) ? superClass.substring(0, p) : "";
        String suffix = (p > 0) ? superClass.substring(p + 1) : superClass;
        String classname = prefix + "." + getSimpleName(ctorParamClass.getName()) + Math.abs(new Random().nextInt()) + suffix;

        ClassPool pool = getClazz().getClassPool();
        CtClass newClass = pool.makeClass(classname);
        newClass.setSuperclass(pool.get(superClass));
        SignatureAttribute.ClassSignature cs = SignatureAttribute.toClassSignature(toDesc(superClass) + "<" + toDesc(ctorParamClass.getName()) + ";>;");
        newClass.setGenericSignature(cs.encode());

        CtConstructor ctor = new CtConstructor(new CtClass[0], newClass);
        ctor.setBody(String.format("{super(%s.class);}", ctorParamClass.getName()));
        newClass.addConstructor(ctor);

        newClass.toClass();

        return classname;
    }
}
