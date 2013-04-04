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

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MemcacheServiceFactoryTransformer extends JavassistTransformer {
    protected void transform(CtClass clazz) throws Exception {
        transformGetMemcacheServiceMethod(clazz);
        transformParameterizedGetMemcacheServiceMethod(clazz);
        transformGetAsyncMemcacheServiceMethod(clazz);
        transformParameterizedGetAsyncMemcacheServiceMethod(clazz);
    }

    private void transformGetMemcacheServiceMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtMethod method = clazz.getDeclaredMethod("getMemcacheService");
        method.setBody(toProxy(MemcacheService.class, "new org.jboss.capedwarf.memcache.CapedwarfMemcacheService()"));
    }

    private void transformParameterizedGetMemcacheServiceMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtMethod method = clazz.getDeclaredMethod("getMemcacheService", new CtClass[]{clazz.getClassPool().get("java.lang.String")});
        method.setBody(toProxy(MemcacheService.class, "new org.jboss.capedwarf.memcache.CapedwarfMemcacheService($1)"));
    }

    private void transformGetAsyncMemcacheServiceMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtMethod method = clazz.getDeclaredMethod("getAsyncMemcacheService");
        method.setBody(toProxy(AsyncMemcacheService.class, "new org.jboss.capedwarf.memcache.CapedwarfAsyncMemcacheService()"));
    }

    private void transformParameterizedGetAsyncMemcacheServiceMethod(CtClass clazz) throws NotFoundException, CannotCompileException {
        CtMethod method = clazz.getDeclaredMethod("getAsyncMemcacheService", new CtClass[]{clazz.getClassPool().get("java.lang.String")});
        method.setBody(toProxy(AsyncMemcacheService.class, "new org.jboss.capedwarf.memcache.CapedwarfAsyncMemcacheService($1)"));
    }
}
