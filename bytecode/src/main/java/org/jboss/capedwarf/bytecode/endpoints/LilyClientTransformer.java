/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.bytecode.endpoints;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import org.jboss.capedwarf.bytecode.RewriteTransformer;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LilyClientTransformer extends RewriteTransformer {
    private static final String INTERNAL = "sendInternal";

    protected void transformInternal(CtClass clazz) throws Exception {
        ClassPool pool = clazz.getClassPool();
        CtClass[] params = new CtClass[]{pool.get("com.google.api.server.spi.tools.devserver.LilyClient$Request")};
        CtMethod send = clazz.getDeclaredMethod("send", params);
        // create new
        CtMethod newMethod = CtNewMethod.copy(send, INTERNAL, clazz, null);
        clazz.addMethod(newMethod);
        // modify original "send"
        send.setBody("{ org.jboss.capedwarf.bytecode.endpoints.LilyClientTransformer.enable(); try { return " + INTERNAL +"($1); } finally { org.jboss.capedwarf.bytecode.endpoints.LilyClientTransformer.disable(); }}");
    }

    protected boolean doCheck(CtClass clazz) throws NotFoundException {
        for (CtMethod m : clazz.getDeclaredMethods()) {
            if (INTERNAL.equals(m.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void enable() {
        Compatibility.enable(Compatibility.Feature.IGNORE_CAPEDWARF_URL_STREAM_HANDLER);
    }

    public static void disable() {
        Compatibility.disable(Compatibility.Feature.IGNORE_CAPEDWARF_URL_STREAM_HANDLER);
    }
}
