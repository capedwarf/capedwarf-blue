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

import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import org.hibernate.search.annotations.*;
import org.jboss.capedwarf.datastore.query.PropertyMapBridge;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class EntityTransformer extends JavassistTransformer {

    @Override
    protected void transform(CtClass clazz) throws Exception {
        addAnnotationsToClass(clazz, ProvidedId.class, Indexed.class);
        addAnnotationToPropertyMapField(clazz);
    }

    private void addAnnotationToPropertyMapField(CtClass clazz) throws NotFoundException {

        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        constPool.addClassInfo(Field.class.getName());
        constPool.addClassInfo(FieldBridge.class.getName());
        constPool.addClassInfo(PropertyMapBridge.class.getName());

        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        attribute.addAnnotation(createFieldAnnotation(constPool));
        attribute.addAnnotation(createFieldBridgeAnnotation(constPool));

        clazz.getDeclaredMethod("getPropertyMap").getMethodInfo().addAttribute(attribute);
    }

    private Annotation createFieldAnnotation(ConstPool constPool) {
//        @Field(analyze=Analyze.YES, store=Store.YES)

        int storeEnumClassIndex = constPool.addUtf8Info(Store.class.getName());
        int yesEnumValueIndex = constPool.addUtf8Info(Store.YES.name());

        int indexEnumClassIndex = constPool.addUtf8Info(Index.class.getName());
        int unTokenizedEnumValueIndex = constPool.addUtf8Info(Index.UN_TOKENIZED.name());

        Annotation annotation = new Annotation(Field.class.getName(), constPool);
        annotation.addMemberValue("index", new EnumMemberValue(indexEnumClassIndex, unTokenizedEnumValueIndex, constPool));
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

    private void addAnnotationsToClass(CtClass clazz, Class<? extends java.lang.annotation.Annotation>... annotationClasses) {
        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);

        for (Class<? extends java.lang.annotation.Annotation> annotationClass : annotationClasses) {
            Annotation annot = new Annotation(annotationClass.getName(), constPool);
            attr.addAnnotation(annot);
        }

        ccFile.addAttribute(attr);
        ccFile.setVersionToJava5();
    }
}
