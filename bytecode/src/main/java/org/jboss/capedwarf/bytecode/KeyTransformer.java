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
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class KeyTransformer extends JavassistTransformer {

    @Override
    protected void transform(CtClass clazz) throws Exception {
        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);

        String transformableClassName = "org.infinispan.query.Transformable";
        String gaeKeyTransformerClassName = "org.jboss.capedwarf.datastore.query.GAEKeyTransformer";

        constPool.addUtf8Info(transformableClassName);
        constPool.addUtf8Info(gaeKeyTransformerClassName);

        Annotation annotation = new Annotation(transformableClassName, constPool);
        annotation.addMemberValue("transformer", new ClassMemberValue(gaeKeyTransformerClassName, constPool));
        attr.addAnnotation(annotation);

        ccFile.addAttribute(attr);
    }

}
