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

import javassist.CtClass;
import javassist.bytecode.annotation.Annotation;
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
public abstract class HibernateSearchAnnotator extends Annotator {

    public HibernateSearchAnnotator(CtClass clazz) {
        super(clazz);
    }

    protected Annotation createFieldAnnotation() {
        return createFieldAnnotation(Index.YES, Analyze.NO, Store.YES);
    }

    protected Annotation createFieldAnnotation(String name) {
        getConstPool().addStringInfo(name);

        Annotation annotation = createFieldAnnotation();
        annotation.addMemberValue("name", memberValueOf(name));
        return annotation;
    }

    protected Annotation createFieldAnnotation(Index index, Analyze analyze, Store store) {
        getConstPool().addClassInfo(Field.class.getName());
        Annotation annotation = createAnnotation(Field.class);
        annotation.addMemberValue("index", createEnumMemberValue(index));
        annotation.addMemberValue("analyze", createEnumMemberValue(analyze));
        annotation.addMemberValue("store", createEnumMemberValue(store));
        return annotation;
    }

    protected Annotation createFieldBridgeAnnotation(Class<? extends org.hibernate.search.bridge.FieldBridge> implClass) {
        Annotation fieldBridge = createAnnotation(FieldBridge.class);
        fieldBridge.addMemberValue("impl", createClassMemberValue(implClass));
        return fieldBridge;
    }

}
