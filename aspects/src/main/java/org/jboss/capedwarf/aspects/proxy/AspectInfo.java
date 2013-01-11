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

package org.jboss.capedwarf.aspects.proxy;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class AspectInfo {
    private Class<?> apiInterface;
    private Object apiImpl;
    private Method method;
    private Object[] params;

    public AspectInfo(Class<?> apiInterface, Object apiImpl, Method method, Object[] params) {
        this.apiInterface = apiInterface;
        this.apiImpl = apiImpl;
        this.method = method;
        this.params = params;
    }

    public Class<?> getApiInterface() {
        return apiInterface;
    }

    public Object getApiImpl() {
        return apiImpl;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParams() {
        return params;
    }
}
