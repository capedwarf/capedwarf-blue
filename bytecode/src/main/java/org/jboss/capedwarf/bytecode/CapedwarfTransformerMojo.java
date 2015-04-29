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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.maven.plugins.transformer.TransformerUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfTransformerMojo {

    public static void main(String[] args) {
        for (String pathToJar : args) {
            modifyJar(pathToJar);
        }
    }

    private static void modifyJar(String pathToJar) {
        String pathToTransformedJar = pathToJar.replace(".jar", "-capedwarf.jar");
        TransformerUtils.main(createArgs(pathToJar, pathToTransformedJar));

        File jar = new File(pathToJar).getAbsoluteFile();
        File transformedJar = new File(pathToTransformedJar).getAbsoluteFile();

        File moduleXml = new File(jar.getParent(), "module.xml");
        if (moduleXml.exists()) {
            replaceText(moduleXml, jar.getName(), transformedJar.getName());
        }
    }

    private static void replaceText(File file, String oldText, String newText) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String contents = new String(bytes);
            contents = contents.replace(oldText, newText);
            Files.write(file.toPath(), contents.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] createArgs(String pathToAppEngineJar, String pathToTransformedAppEngineJar) {
        return new String[]{
                pathToAppEngineJar,
                CapedwarfTransformer.class.getName(),
                "(([.]*ProspectiveSearchServiceFactory*)" +
                        "|([.]*labs.modules.ModulesServiceFactory.class*)" +
                        "|([.]*apphosting.api.ApiProxy.class*)" +
                        "|([.]*datastore.Cursor*)" +
                        "|([.]*datastore.DatastoreServiceConfig*)" +
                        "|([.]*datastore.RawValue*)" +
                        "|([.]*datastore.Entity.class*)" +
                        "|([.]*datastore.Key.class*)" +
                        "|([.]*search.GeoPoint.class*)" +
                        "|([.]*backend.QueryInterceptor.class*)" +
                        "|([.]*cloud.sql.jdbc.Driver.class*))",
                pathToTransformedAppEngineJar};
    }
}
