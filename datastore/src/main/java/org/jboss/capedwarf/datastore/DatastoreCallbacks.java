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
import org.jboss.capedwarf.shared.reflection.MethodInvocation;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * Datastore callbacks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class DatastoreCallbacks {
    private static final Class<?> ctpClass;

    static Class<?> getCurrentTransactionProviderClass() {
        try {
            return DatastoreCallbacks.class.getClassLoader().loadClass("com.google.appengine.api.datastore.CurrentTransactionProvider");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static {
        ctpClass = getCurrentTransactionProviderClass();
    }

    private final MethodInvocation executePrePutCallbacks;
    private final MethodInvocation executePostPutCallbacks;
    private final MethodInvocation executePreDeleteCallbacks;
    private final MethodInvocation executePostDeleteCallbacks;
    private final MethodInvocation executePreGetCallbacks;
    private final MethodInvocation executePostLoadCallbacks;
    private final MethodInvocation executePreQueryCallbacks;

    DatastoreCallbacks(Object callbacks) {
        executePrePutCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePrePutCallbacks", PutContext.class);
        executePostPutCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePostPutCallbacks", PutContext.class);
        executePreDeleteCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePreDeleteCallbacks", DeleteContext.class);
        executePostDeleteCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePostDeleteCallbacks", DeleteContext.class);
        executePreGetCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePreGetCallbacks", PreGetContext.class);
        executePostLoadCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePostLoadCallbacks", PostLoadContext.class);
        executePreQueryCallbacks = ReflectionUtils.cacheTargetMethod(callbacks, "executePreQueryCallbacks", PreQueryContext.class);
    }

    private static void execute(MethodInvocation mi, Object context) {
        mi.invoke(context);
    }

    private static Object getCurrentTransactionProvider(final CurrentTransactionProvider ctp) {
        return Proxy.newProxyInstance(ctpClass.getClassLoader(), new Class<?>[]{ctpClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String name = method.getName();
                if ("toString".equals(name)) {
                    return "<CurrentTransactionProvider>";
                } else if ("equals".equals(name)) {
                    return (args[0] == proxy);
                } else if ("hashCode".equals(name)) {
                    return ctp.hashCode();
                } else {
                    return ctp.getCurrentTransaction(Transaction.class.cast(args[0]));
                }
            }
        });
    }

    private static PutContext createPutContext(final CurrentTransactionProvider ctp, List<Entity> entities) {
        Class[] types = {ctpClass, List.class};
        Object[] args = {getCurrentTransactionProvider(ctp), entities};
        return ReflectionUtils.newInstance(PutContext.class, types, args);
    }

    private static DeleteContext createDeleteContext(final CurrentTransactionProvider ctp, List<Key> keys) {
        Class[] types = {ctpClass, List.class};
        Object[] args = {getCurrentTransactionProvider(ctp), keys};
        return ReflectionUtils.newInstance(DeleteContext.class, types, args);
    }

    private static PreGetContext createPreGetContext(final CurrentTransactionProvider ctp, List<Key> keys, Map<Key,Entity> resultMap) {
        Class[] types = {ctpClass, List.class, Map.class};
        Object[] args = {getCurrentTransactionProvider(ctp), keys, resultMap};
        return ReflectionUtils.newInstance(PreGetContext.class, types, args);
    }

    private static PostLoadContext createPostLoadContext(final CurrentTransactionProvider ctp, Entity result) {
        Class[] types = {ctpClass, Entity.class};
        Object[] args = {getCurrentTransactionProvider(ctp), result};
        return ReflectionUtils.newInstance(PostLoadContext.class, types, args);
    }

    private static PostLoadContext createPostLoadContext(final CurrentTransactionProvider ctp, List<Entity> results) {
        Class[] types = {ctpClass, List.class};
        Object[] args = {getCurrentTransactionProvider(ctp), results};
        return ReflectionUtils.newInstance(PostLoadContext.class, types, args);
    }

    private static PreQueryContext createPreQueryContext(final CurrentTransactionProvider ctp, Query query) {
        Class[] types = {ctpClass, Query.class};
        Object[] args = {getCurrentTransactionProvider(ctp), query};
        return ReflectionUtils.newInstance(PreQueryContext.class, types, args);
    }

    void executePrePutCallbacks(CurrentTransactionProvider ctp, List<Entity> entities) {
        final PutContext context = createPutContext(ctp, entities);
        executePrePutCallbacks(context);
    }

    private void executePrePutCallbacks(PutContext putContext) {
        execute(executePrePutCallbacks, putContext);
    }

    void executePostPutCallbacks(CurrentTransactionProvider ctp, List<Entity> entities) {
        final PutContext context = createPutContext(ctp, entities);
        executePostPutCallbacks(context);
    }

    private void executePostPutCallbacks(PutContext putContext) {
        execute(executePostPutCallbacks, putContext);
    }

    void executePreDeleteCallbacks(CurrentTransactionProvider ctp, List<Key> keys) {
        final DeleteContext context = createDeleteContext(ctp, keys);
        executePreDeleteCallbacks(context);
    }

    private void executePreDeleteCallbacks(DeleteContext deleteContext) {
        execute(executePreDeleteCallbacks, deleteContext);
    }

    void executePostDeleteCallbacks(CurrentTransactionProvider ctp, List<Key> keys) {
        final DeleteContext context = createDeleteContext(ctp, keys);
        executePostDeleteCallbacks(context);
    }

    private void executePostDeleteCallbacks(DeleteContext deleteContext) {
        execute(executePostDeleteCallbacks, deleteContext);
    }

    void executePreGetCallbacks(CurrentTransactionProvider ctp, List<Key> keys, Map<Key,Entity> resultMap) {
        final PreGetContext context = createPreGetContext(ctp, keys, resultMap);
        executePreGetCallbacks(context);
    }

    private void executePreGetCallbacks(PreGetContext preGetContext) {
        execute(executePreGetCallbacks, preGetContext);
    }

    void executePostLoadCallbacks(CurrentTransactionProvider ctp, Entity entity) {
        final PostLoadContext context = createPostLoadContext(ctp, entity);
        executePostLoadCallbacks(context);
    }

    void executePostLoadCallbacks(CurrentTransactionProvider ctp, List<Entity> entities) {
        final PostLoadContext context = createPostLoadContext(ctp, entities);
        executePostLoadCallbacks(context);
    }

    private void executePostLoadCallbacks(PostLoadContext postLoadContext) {
        execute(executePostLoadCallbacks, postLoadContext);
    }

    void executePreQueryCallbacks(CurrentTransactionProvider ctp, Query query) {
        final PreQueryContext context = createPreQueryContext(ctp, query);
        executePreQueryCallbacks(context);
    }

    private void executePreQueryCallbacks(PreQueryContext preQueryContext) {
        execute(executePreQueryCallbacks, preQueryContext);
    }
}
