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

package org.jboss.capedwarf.bytecode;

import java.util.LinkedList;
import java.util.List;

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
import org.hibernate.search.annotations.Store;

/**
 * Helper superclass for annotation classes with hibernate-search annotations (to make the class indexable).
 * Subclasses should implement method addAnnotations() and call addAnnotationsToClass/Method/Field.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public abstract class HibernateSearchAnnotator {

    private CtClass clazz;
    private ClassFile classFile;
    private ConstPool constPool;

    public HibernateSearchAnnotator(CtClass clazz) {
        this.clazz = clazz;
        classFile = clazz.getClassFile();
        constPool = classFile.getConstPool();
    }

    protected CtClass getClazz() {
        return clazz;
    }

    protected ClassFile getClassFile() {
        return classFile;
    }

    protected ConstPool getConstPool() {
        return constPool;
    }

    public abstract void addAnnotations() throws Exception;

    protected Annotation createFieldAnnotation() {
        return createFieldAnnotation(Index.YES, Analyze.NO, Store.YES);
    }

    protected Annotation createFieldAnnotation(String name) {
        constPool.addStringInfo(name);

        Annotation annotation = createFieldAnnotation();
        annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        return annotation;
    }

    protected Annotation createFieldAnnotation(Index index, Analyze analyze, Store store) {
        constPool.addClassInfo(Field.class.getName());
        Annotation annotation = createAnnotation(Field.class);
        annotation.addMemberValue("index", createEnumMemberValue(index));
        annotation.addMemberValue("analyze", createEnumMemberValue(analyze));
        annotation.addMemberValue("store", createEnumMemberValue(store));
        return annotation;
    }

    private EnumMemberValue createEnumMemberValue(Enum enumValue) {
        int enumClassIndex = constPool.addUtf8Info(enumValue.getDeclaringClass().getName());
        int enumValueIndex = constPool.addUtf8Info(enumValue.name());
        return new EnumMemberValue(enumClassIndex, enumValueIndex, constPool);
    }

    protected Annotation createFieldBridgeAnnotation(Class<? extends org.hibernate.search.bridge.FieldBridge> implClass) {
        Annotation fieldBridge = createAnnotation(FieldBridge.class);
        fieldBridge.addMemberValue("impl", createClassMemberValue(implClass));
        return fieldBridge;
    }

    protected ClassMemberValue createClassMemberValue(Class<?> clazz) {
        return new ClassMemberValue(clazz.getName(), constPool);
    }

    protected void addAnnotationsToClass(Class<? extends java.lang.annotation.Annotation>... annotationClasses) {
        addAnnotationsToClass(createAnnotations(annotationClasses));
    }

    protected void addAnnotationsToClass(Annotation... annotations) {
        classFile.addAttribute(createAnnotationAttribute(annotations));
    }

    protected void addAnnotationsToMethod(String methodName, Annotation... annotations) throws NotFoundException {
        clazz.getDeclaredMethod(methodName).getMethodInfo().addAttribute(createAnnotationAttribute(annotations));
    }

    protected void addAnnotationsToField(String fieldName, Annotation... annotations) throws NotFoundException {
        clazz.getDeclaredField(fieldName).getFieldInfo().addAttribute(createAnnotationAttribute(annotations));
    }

    private Annotation[] createAnnotations(Class<? extends java.lang.annotation.Annotation>[] annotationClasses) {
        List<Annotation> annotationList = new LinkedList<Annotation>();
        for (Class<? extends java.lang.annotation.Annotation> annotationClass : annotationClasses) {
            annotationList.add(createAnnotation(annotationClass));
        }
        return annotationList.toArray(new Annotation[annotationList.size()]);
    }

    protected Annotation createAnnotation(Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return new Annotation(annotationClass.getName(), constPool);
    }

    private AnnotationsAttribute createAnnotationAttribute(Annotation... annotations) {
        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        for (Annotation annotation : annotations) {
            attribute.addAnnotation(annotation);
        }
        return attribute;
    }

}
