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

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CapedwarfVelocityContext extends AbstractContext {

    private Map<String, Object> map = new HashMap<String, Object>();

    private BeanManager manager;

    private static ThreadLocal<CapedwarfVelocityContext> threadLocal = new ThreadLocal<CapedwarfVelocityContext>();
    private HttpServletRequest req;

    public CapedwarfVelocityContext() {
    }

    public CapedwarfVelocityContext(BeanManager manager, HttpServletRequest request) {
        assert manager != null : "manager should not be null";
        assert request != null : "request should not be null";
        this.manager = manager;
        init(request);
    }

    public void init(HttpServletRequest request) {
        this.req = request;
        put("request", request);
    }

    @Override
    public Object internalGet(String key) {
        Object value = map.get(key);
        if (value != null)
            return value;

        Bean<? extends Object> bean = manager.resolve(manager.getBeans(key));
        if (bean == null)
            return null;
        
        CreationalContext<? extends Object> creationalContext = manager.createCreationalContext(bean);
        return manager.getReference(bean, Object.class, creationalContext);
    }

    @Override
    public Object internalPut(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public boolean internalContainsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Object[] internalGetKeys() {
        return new ArrayList<Object>(map.keySet()).toArray();
    }

    @Override
    public Object internalRemove(Object key) {
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
        threadLocal.set(null);
    }

    public HttpServletRequest getRequest() {
        return req;
    }
}
