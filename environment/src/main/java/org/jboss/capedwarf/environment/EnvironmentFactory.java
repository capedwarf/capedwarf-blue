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

package org.jboss.capedwarf.environment;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

/**
 * Env factory.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public final class EnvironmentFactory {

    private static Environment environment;

    /**
     * Get environment.
     *
     * We use ServiceLoader pattern.
     * Proper env impls should be used for diff envs.
     *
     * @return the environment
     */
    public synchronized static Environment getEnvironment() {
        if (environment == null)
            environment = findEnvironment();
        return environment;
    }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private static Environment findEnvironment() {
        final ClassLoader cl = Environment.class.getClassLoader();
        final ServiceLoader<Environment> envs = ServiceLoader.load(Environment.class, cl);
        for (Environment env : envs)
            return env;
    
        Logger.getLogger(EnvironmentFactory.class.getName()).warning("No Environment service present, using NoopEnv!");
        return new NoopEnv();
    }

    private static class NoopEnv extends AbstractEnvironment {
        private AtomicLong nextId = new AtomicLong(1);
        private AtomicLong txId = new AtomicLong(Long.MIN_VALUE);

        public String getDomain() {
            return "dummy";
        }

        public Long getRange(String appId, Key parent, String sequenceName, long num) {
            return nextId.getAndAdd(num);
        }

        public DatastoreService.KeyRangeState checkRange(String appId, KeyRange keyRange, String sequenceName) {
            long start = keyRange.getStart().getId();
            long next = nextId.get();
            // no support for empty atm
            return start < next ? DatastoreService.KeyRangeState.COLLISION : DatastoreService.KeyRangeState.CONTENTION;
        }

        public String getTransactionId() {
            return String.valueOf(txId.getAndIncrement());
        }
    }
}
