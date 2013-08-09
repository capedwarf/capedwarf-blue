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
 */
public class CapedwarfTransformerMojo {

    public static void main(String[] args) {
        String pathToAppEngineJar = args[0];
        String pathToTransformedAppEngineJar = pathToAppEngineJar.replace(".jar", "-capedwarf.jar");
        TransformerUtils.main(createArgs(pathToAppEngineJar, pathToTransformedAppEngineJar));

        File appEngineJar = new File(pathToAppEngineJar).getAbsoluteFile();
        File transformedAppEngineJar = new File(pathToTransformedAppEngineJar).getAbsoluteFile();

        File moduleXml = new File(appEngineJar.getParent(), "module.xml");
        if (moduleXml.exists()) {
            replaceText(moduleXml, appEngineJar.getName(), transformedAppEngineJar.getName());
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
                        "|([.]*apphosting.api.ApiProxy.class*)" +
                        "|([.]*datastore.Cursor*)" +
                        "|([.]*datastore.DatastoreServiceConfig*)" +
                        "|([.]*datastore.RawValue*)" +
                        "|([.]*datastore.Entity.class*)" +
                        "|([.]*datastore.Key.class*)" +
                        "|([.]*cloud.sql.jdbc.Driver.class*))",
                pathToTransformedAppEngineJar};
    }
}
