/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class FactoriesTransformer implements ClassFileTransformer {

    private static Logger log = Logger.getLogger(FactoriesTransformer.class.getName());

    private static Map<String, ClassFileTransformer> transformers = new HashMap<String, ClassFileTransformer>();

    // -- Keep lexicographical order --

    static {
        register("com.google.appengine.api.blobstore.BlobstoreServiceFactory", new BlobstoreServiceFactoryTransformer());
        register("com.google.appengine.api.capabilities.CapabilitiesServiceFactory", new CapabilitiesServiceFactoryTransformer());
        register("com.google.appengine.api.datastore.DatastoreServiceFactory", new DatastoreServiceFactoryTransformer());
        register("com.google.appengine.api.datastore.Entity", new EntityTransformer());
        register("com.google.appengine.api.datastore.Key", new KeyTransformer());
        register("com.google.appengine.api.files.FileServiceFactory", new FileServiceFactoryTransformer());
        register("com.google.appengine.api.images.ImagesServiceFactory", new ImagesServiceFactoryTransformer());
        register("com.google.appengine.api.mail.MailServiceFactory", new MailServiceFactoryTransformer());
        register("com.google.appengine.api.memcache.MemcacheServiceFactory", new MemcacheServiceFactoryTransformer());
        register("com.google.appengine.api.urlfetch.URLFetchServiceFactory", new URLFetchServiceFactoryTransformer());
        register("com.google.appengine.api.xmpp.XMPPServiceFactory", new XMPPServiceFactoryTransformer());
        register("com.google.appengine.api.users.UserServiceFactory", new UserServiceFactoryTransformer());
        register("com.google.apphosting.api.ApiProxy", new ApiProxyTransformer());
    }

    private static void register(String fullName, ClassFileTransformer transformer) {
        transformers.put(fullName, transformer);
        transformers.put(convertToPath(fullName), transformer);
    }

    private static String convertToPath(String fullName) {
        return fullName.replace(".", "/");
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassFileTransformer cft = transformers.get(className);
        if (cft != null)
            return cft.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);

        return classfileBuffer;
    }
}
