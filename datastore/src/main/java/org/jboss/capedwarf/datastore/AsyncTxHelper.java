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

import java.util.concurrent.Future;

/**
 * Using impl details to async commit / rollback tx.
 * e.g. expecting tx param to be of BaseTransaction instance
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings({"unchecked", "UnusedParameters"})
final class AsyncTxHelper {
    private static final String GET_TM = "getTransactionManager";
    private static final String COMMIT_ASYNC = "commitAsync";

    static Future<Void> commit(Object tx) {
        /*
        if (tx != null) {
            try {
                if (tx instanceof TransactionManager) {
                    tx = ReflectionUtils.invokeStaticMethod(tx.getClass(), GET_TM);
                }
                return (Future<Void>) ReflectionUtils.invokeInstanceMethod(tx, COMMIT_ASYNC);
            } catch (Exception ignore) {
            }
        }
        */
        return null;
    }

    static Future<Void> rollback(Object tx) {
        return null;
    }
}