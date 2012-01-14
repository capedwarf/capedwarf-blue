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

package org.jboss.capedwarf.bytecode.datanucleus;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.jboss.capedwarf.bytecode.JavassistTransformer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fix VFS3 usage.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class NonManagedPluginRegistryTransformer extends JavassistTransformer {

    private static final String VFS3_FIX = "final java.net.URL vfs3fix = " + NonManagedPluginRegistryTransformer.class.getName() + ".fixVFS3($0);" +
                                           "if (vfs3fix != null) return vfs3fix;";

    protected void transform(CtClass clazz) throws Exception {
        final ClassPool pool = clazz.getClassPool();
        final CtMethod method = clazz.getDeclaredMethod("getManifestURL", new CtClass[]{pool.get(URL.class.getName())});
        method.insertBefore(VFS3_FIX);
    }
    
    public static URL fixVFS3(final URL pluginURL) {
        if (pluginURL != null && pluginURL.toString().startsWith("vfs:"))
        {
            String urlStr = pluginURL.toString().replace("plugin.xml", "META-INF/MANIFEST.MF");
            try
            {
                return new URL(urlStr);
            }
            catch (MalformedURLException e)
            {
                Logger.getLogger(NonManagedPluginRegistryTransformer.class.getName()).log(Level.WARNING, "Error while applying VFS3 fix.", e);
            }
        }
        return null;
    }
}
