/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.bytecode;

import com.google.appengine.api.datastore.Entity;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;
import org.hibernate.search.annotations.Store;
import org.jboss.capedwarf.datastore.query.PropertyMapBridge;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class EntityTransformer extends JavassistTransformer {

    @Override
    protected void transform(CtClass clazz) throws Exception {
        annotateClass(clazz, ProvidedId.class, Indexed.class);
        annotateGetKindMethod(clazz);
        annotateGetPropertyMapMethod(clazz);
    }

    private void annotateGetKindMethod(CtClass clazz) throws NotFoundException {
        ConstPool constPool = getConstPool(clazz);
        constPool.addClassInfo(Field.class.getName());

        Annotation fieldAnnotation = createFieldAnnotation(Entity.KEY_RESERVED_PROPERTY, constPool);
        addAnnotationsToMethod(clazz, "getKind", fieldAnnotation);
    }

    private void addAnnotationsToMethod(CtClass clazz, String methodName, Annotation... annotations) throws NotFoundException {
        AnnotationsAttribute attribute = createAnnotationAttribute(clazz, annotations);
        clazz.getDeclaredMethod(methodName).getMethodInfo().addAttribute(attribute);
    }

    private AnnotationsAttribute createAnnotationAttribute(CtClass clazz, Annotation... annotations) {
        AnnotationsAttribute attribute = new AnnotationsAttribute(getConstPool(clazz), AnnotationsAttribute.visibleTag);
        for (Annotation annotation : annotations) {
            attribute.addAnnotation(annotation);
        }
        return attribute;
    }

    private ConstPool getConstPool(CtClass clazz) {
        ClassFile ccFile = clazz.getClassFile();
        return ccFile.getConstPool();
    }

    private void annotateGetPropertyMapMethod(CtClass clazz) throws NotFoundException {
        ConstPool constPool = getConstPool(clazz);
//        constPool.addClassInfo(Field.class.getName());        IS THIS NECESSARY OR NOT!?  APPARENTLY NOT
//        constPool.addClassInfo(FieldBridge.class.getName());
//        constPool.addClassInfo(PropertyMapBridge.class.getName());

        Annotation fieldAnnotation = createFieldAnnotation(constPool);
        Annotation fieldBridgeAnnotation = createFieldBridgeAnnotation(constPool);
        addAnnotationsToMethod(clazz, "getPropertyMap", fieldAnnotation, fieldBridgeAnnotation);
    }

    private Annotation createFieldAnnotation(String name, ConstPool constPool) {
        constPool.addStringInfo(name);

        Annotation annotation = createFieldAnnotation(constPool);
        annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        return annotation;
    }

    private Annotation createFieldAnnotation(ConstPool constPool) {
        int storeEnumClassIndex = constPool.addUtf8Info(Store.class.getName());
        int yesEnumValueIndex = constPool.addUtf8Info(Store.YES.name());

        int indexEnumClassIndex = constPool.addUtf8Info(Index.class.getName());
        int yesTokenizedEnumValueIndex = constPool.addUtf8Info(Index.YES.name());

        int analyzeEnumClassIndex = constPool.addUtf8Info(Analyze.class.getName());
        int noTokenizedEnumValueAnalyze = constPool.addUtf8Info(Analyze.NO.name());

        Annotation annotation = new Annotation(Field.class.getName(), constPool);
        annotation.addMemberValue("index", new EnumMemberValue(indexEnumClassIndex, yesTokenizedEnumValueIndex, constPool));
        annotation.addMemberValue("analyze", new EnumMemberValue(analyzeEnumClassIndex, noTokenizedEnumValueAnalyze, constPool));
        annotation.addMemberValue("store", new EnumMemberValue(storeEnumClassIndex, yesEnumValueIndex, constPool));
        return annotation;
    }

    /**
     * Creates the following annotation: @FieldBridge(impl=PropertyMapBridge.class)
     *
     * @param constPool constant pool
     * @return the @FieldBridge annotation
     */
    private Annotation createFieldBridgeAnnotation(ConstPool constPool) {

        Annotation fieldBridgeAnnotation = new Annotation(FieldBridge.class.getName(), constPool);
        fieldBridgeAnnotation.addMemberValue("impl", new ClassMemberValue(PropertyMapBridge.class.getName(), constPool));
        return fieldBridgeAnnotation;
    }

    private void annotateClass(CtClass clazz, Class<? extends java.lang.annotation.Annotation>... annotationClasses) {
        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        Annotation[] annotations = createAnnotations(constPool, annotationClasses);
        ccFile.addAttribute(createAnnotationAttribute(clazz, annotations));
    }

    private Annotation[] createAnnotations(ConstPool constPool, Class<? extends java.lang.annotation.Annotation>[] annotationClasses) {
        List<Annotation> annotationList = new LinkedList<Annotation>();
        for (Class<? extends java.lang.annotation.Annotation> annotationClass : annotationClasses) {
            annotationList.add(new Annotation(annotationClass.getName(), constPool));
        }
        return annotationList.toArray(new Annotation[annotationList.size()]);
    }
}
