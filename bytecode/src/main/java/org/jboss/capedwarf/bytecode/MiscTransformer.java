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

/**
 * Non service factory transformers go here.
 * (service factory transfomers go to FactoriesTransfomer)
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
final class MiscTransformer extends MultipleTransformer {

    // -- Keep lexicographical order --

    public MiscTransformer() {
        // GAE apphosting
        register("com.google.apphosting.api.ApiProxy", new ApiProxyTransformer());
        // GAE API
        register("com.google.appengine.api.datastore.Cursor", new CursorTransformer());
        register("com.google.appengine.api.datastore.DatastoreServiceConfig$Builder", new DatastoreServiceConfigBuilderTransformer());
        register("com.google.appengine.api.datastore.Entity", new EntityTransformer());
        register("com.google.appengine.api.datastore.Key", new KeyTransformer());
        register("com.google.appengine.api.datastore.RawValue", new RawValueTransformer());
        // GAE MapReduce
        register("com.google.appengine.tools.mapreduce.impl.ShuffleServiceImpl", new ShuffleServiceTransformer());
        register("com.google.appengine.tools.mapreduce.impl.ShuffleJob", new ShuffleJobTransformer());
        // GAE DN plugin
        register("com.google.appengine.tools.development.testing.LocalServiceTestHelper", new LocalServiceTestHelperTransformer());
    }
}
