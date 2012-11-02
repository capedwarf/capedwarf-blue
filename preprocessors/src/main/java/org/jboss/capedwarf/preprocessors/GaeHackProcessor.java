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

package org.jboss.capedwarf.preprocessors;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.appengine.tools.compilation.DatastoreCallbacksProcessor;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SupportedAnnotationTypes({
        "com.google.appengine.api.datastore.PrePut",
        "com.google.appengine.api.datastore.PostPut",
        "com.google.appengine.api.datastore.PreDelete",
        "com.google.appengine.api.datastore.PostDelete",
        "com.google.appengine.api.datastore.PreGet",
        "com.google.appengine.api.datastore.PostLoad",
        "com.google.appengine.api.datastore.PreQuery"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({"debug"})
@MetaInfServices(Processor.class)
public class GaeHackProcessor extends DatastoreCallbacksProcessor {

    private static final String CALLBACKS_FILE = "META-INF/datastorecallbacks.xml";

    private Field callbacksConfigWriterField;
    private Field configOutputStream;

    public GaeHackProcessor() throws Exception {
        // writer
        callbacksConfigWriterField = DatastoreCallbacksProcessor.class.getDeclaredField("callbacksConfigWriter");
        callbacksConfigWriterField.setAccessible(true);
        // output stream
        configOutputStream = DatastoreCallbacksProcessor.class.getDeclaredField("configOutputStream");
        configOutputStream.setAccessible(true);
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // TODO -- why it doesn't write down classes
        final String osName = System.getProperty("os.name");
        if (osName.contains("Windows") == false)
            return false;

        try {
            final Object writter = loadCallbacksConfigWriter();
            if (writter != null) {
                callbacksConfigWriterField.set(this, writter);
            }

            if (roundEnv.processingOver()) {
                final Filer filer = processingEnv.getFiler();
                final FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", CALLBACKS_FILE);
                configOutputStream.set(this, fileObject.openOutputStream());
            }

            final boolean process = super.process(annotations, roundEnv);
            if (process) {
                Logger.getLogger(GaeHackProcessor.class.getName()).info("Hacked around GAE DatastoreCallbacksProcessor Winz bug. ;-)");
            }
            return process;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object loadCallbacksConfigWriter() {
        try {
            final FileObject existingFile = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", CALLBACKS_FILE);
            if (existingFile != null) {
                InputStream inputStream = null;
                try {
                    inputStream = existingFile.openInputStream();
                } catch (IOException ignored) {
                }
                try {
                    final Class<?> clazz = getClass().getClassLoader().loadClass("com.google.appengine.tools.compilation.DatastoreCallbacksConfigWriter");
                    final Constructor<?> constructor = clazz.getDeclaredConstructor(InputStream.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to read %s", CALLBACKS_FILE), e);
        }
    }
}
