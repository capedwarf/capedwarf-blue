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

package org.jboss.capedwarf.datastore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DeleteContext;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PostLoadContext;
import com.google.appengine.api.datastore.PreGetContext;
import com.google.appengine.api.datastore.PreQueryContext;
import com.google.appengine.api.datastore.PutContext;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.capedwarf.common.reflection.MethodInvocation;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

/**
 * Datastore callbacks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class DatastoreCallbacks {
    private final MethodInvocation executePrePutCallbacks;
    private final MethodInvocation executePostPutCallbacks;
    private final MethodInvocation executePreDeleteCallbacks;
    private final MethodInvocation executePostDeleteCallbacks;
    private final MethodInvocation executePreGetCallbacks;
    private final MethodInvocation executePostLoadCallbacks;
    private final MethodInvocation executePreQueryCallbacks;

    DatastoreCallbacks(Object callbacks) {
        executePrePutCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePrePutCallbacks", PutContext.class);
        executePostPutCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePostPutCallbacks", PutContext.class);
        executePreDeleteCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePreDeleteCallbacks", DeleteContext.class);
        executePostDeleteCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePostDeleteCallbacks", DeleteContext.class);
        executePreGetCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePreGetCallbacks", PreGetContext.class);
        executePostLoadCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePostLoadCallbacks", PostLoadContext.class);
        executePreQueryCallbacks = ReflectionUtils.cacheMethod(callbacks, "executePreQueryCallbacks", PreQueryContext.class);
    }

    private static void execute(MethodInvocation mi, Object context) {
        try {
            mi.invoke(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Class<?> getCurrentTransactionProviderClass() {
        try {
            return DatastoreCallbacks.class.getClassLoader().loadClass("com.google.appengine.api.datastore.CurrentTransactionProvider");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static Object getCurrentTransactionProvider(final Transaction tx) {
        return Proxy.newProxyInstance(DatastoreCallbacks.class.getClassLoader(), new Class<?>[]{getCurrentTransactionProviderClass()}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String name = method.getName();
                if ("toString".equals(name)) {
                    return "<CurrentTransactionProvider>";
                } else if ("equals".equals(name)) {
                    return (args[0] == proxy);
                } else if ("hashCode".equals(name)) {
                    return tx.hashCode();
                } else {
                    return tx;
                }
            }
        });
    }

    static PutContext createPutContext(final Transaction tx, List<Entity> entities) {
        Class[] types = {getCurrentTransactionProviderClass(), List.class};
        Object[] args = {getCurrentTransactionProvider(tx), entities};
        return ReflectionUtils.newInstance(PutContext.class, types, args);
    }

    static DeleteContext createDeleteContext(final Transaction tx, List<Key> keys) {
        Class[] types = {getCurrentTransactionProviderClass(), List.class};
        Object[] args = {getCurrentTransactionProvider(tx), keys};
        return ReflectionUtils.newInstance(DeleteContext.class, types, args);
    }

    static PreGetContext createPreGetContext(final Transaction tx, List<Key> keys, Map<Key,Entity> resultMap) {
        Class[] types = {getCurrentTransactionProviderClass(), List.class, Map.class};
        Object[] args = {getCurrentTransactionProvider(tx), keys, resultMap};
        return ReflectionUtils.newInstance(PreGetContext.class, types, args);
    }

    static PostLoadContext createPostLoadContext(final Transaction tx, Entity result) {
        Class[] types = {getCurrentTransactionProviderClass(), Entity.class};
        Object[] args = {getCurrentTransactionProvider(tx), result};
        return ReflectionUtils.newInstance(PostLoadContext.class, types, args);
    }

    static PostLoadContext createPostLoadContext(final Transaction tx, List<Entity> results) {
        Class[] types = {getCurrentTransactionProviderClass(), List.class};
        Object[] args = {getCurrentTransactionProvider(tx), results};
        return ReflectionUtils.newInstance(PostLoadContext.class, types, args);
    }

    static PreQueryContext createPreQueryContext(final Transaction tx, Query query) {
        Class[] types = {getCurrentTransactionProviderClass(), Query.class};
        Object[] args = {getCurrentTransactionProvider(tx), query};
        return ReflectionUtils.newInstance(PreQueryContext.class, types, args);
    }

    void executePrePutCallbacks(PutContext putContext) {
        execute(executePrePutCallbacks, putContext);
    }

    void executePostPutCallbacks(PutContext putContext) {
        execute(executePostPutCallbacks, putContext);
    }

    void executePreDeleteCallbacks(DeleteContext deleteContext) {
        execute(executePreDeleteCallbacks, deleteContext);
    }

    void executePostDeleteCallbacks(DeleteContext deleteContext) {
        execute(executePostDeleteCallbacks, deleteContext);
    }

    void executePreGetCallbacks(PreGetContext preGetContext) {
        execute(executePreGetCallbacks, preGetContext);
    }

    void executePostLoadCallbacks(PostLoadContext postLoadContext) {
        execute(executePostLoadCallbacks, postLoadContext);
    }

    void executePreQueryCallbacks(PreQueryContext preQueryContext) {
        execute(executePreQueryCallbacks, preQueryContext);
    }
}
