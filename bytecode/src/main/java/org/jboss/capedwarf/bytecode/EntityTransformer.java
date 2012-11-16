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
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;
import org.jboss.capedwarf.datastore.query.EntityKeyBridge;
import org.jboss.capedwarf.datastore.query.NamespaceBridge;
import org.jboss.capedwarf.datastore.query.PropertyMapBridge;
import org.jboss.capedwarf.datastore.query.QueryConverter;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class EntityTransformer extends JavassistTransformer {

    @Override
    protected void transform(CtClass clazz) throws Exception {
        new EntityAnnotator(clazz).addAnnotations();
    }

    private class EntityAnnotator extends HibernateSearchAnnotator {
        private EntityAnnotator(CtClass clazz) {
            super(clazz);
        }

        @Override
        public void addAnnotations() throws Exception {
            //noinspection unchecked
            addAnnotationsToClass(ProvidedId.class, Indexed.class);
            addAnnotationsToMethod("getKind", createFieldAnnotation(QueryConverter.KIND_PROPERTY_KEY));
            addAnnotationsToMethod("getNamespace", createFieldAnnotation(QueryConverter.NAMESPACE_PROPERTY_KEY), createFieldBridgeAnnotation(NamespaceBridge.class));
            addAnnotationsToMethod("getKey", createFieldAnnotation(Entity.KEY_RESERVED_PROPERTY), createFieldBridgeAnnotation(EntityKeyBridge.class));
            addAnnotationsToField("propertyMap", createFieldAnnotation(), createFieldBridgeAnnotation(PropertyMapBridge.class));
        }
    }
}
