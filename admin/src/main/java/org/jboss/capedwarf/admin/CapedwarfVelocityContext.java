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

package org.jboss.capedwarf.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.tools.generic.EscapeTool;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfVelocityContext extends AbstractContext {

    private Map<String, Object> map = new HashMap<String, Object>();

    private BeanManager manager;

    private static ThreadLocal<CapedwarfVelocityContext> threadLocal = new ThreadLocal<CapedwarfVelocityContext>();
    private HttpServletRequest req;

    @SuppressWarnings("unused")
    public CapedwarfVelocityContext() {
    }

    public CapedwarfVelocityContext(BeanManager manager, HttpServletRequest request) {
        if (manager == null) {
            throw new IllegalArgumentException("Null bean manager!");
        }
        if (request == null) {
            throw new IllegalArgumentException("Null request!");
        }
        this.manager = manager;
        init(request);
    }

    public void init(HttpServletRequest request) {
        this.req = request;
        put("base_url", CapedwarfEnvironment.getThreadLocalInstance().getBaseApplicationUrl());
        put("request", request);
        put("esc", new EscapeTool());
    }

    @Override
    public Object internalGet(String key) {
        Object value = map.get(key);
        if (value != null)
            return value;

        Bean<?> bean = manager.resolve(manager.getBeans(key));
        if (bean == null)
            return null;

        CreationalContext<?> creationalContext = manager.createCreationalContext(bean);
        return manager.getReference(bean, Object.class, creationalContext);
    }

    @Override
    public Object internalPut(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public boolean internalContainsKey(Object key) {
        //noinspection SuspiciousMethodCalls
        return map.containsKey(key);
    }

    @Override
    public Object[] internalGetKeys() {
        return new ArrayList<Object>(map.keySet()).toArray();
    }

    @Override
    public Object internalRemove(Object key) {
        //noinspection SuspiciousMethodCalls
        return map.remove(key);
    }

    public static CapedwarfVelocityContext getInstance() {
        return threadLocal.get();
    }

    public static CapedwarfVelocityContext createThreadLocalInstance(BeanManager manager, HttpServletRequest req) {
        CapedwarfVelocityContext capedwarfVelocityContext = new CapedwarfVelocityContext(manager, req);
        threadLocal.set(capedwarfVelocityContext);
        return capedwarfVelocityContext;
    }

    public static void clearThreadLocalInstance() {
        threadLocal.remove();
    }

    public HttpServletRequest getRequest() {
        return req;
    }
}
