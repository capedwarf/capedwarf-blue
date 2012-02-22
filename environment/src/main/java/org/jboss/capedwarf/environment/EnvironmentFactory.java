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

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;

import java.util.ServiceLoader;
import java.util.logging.Logger;

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
    
        Logger.getLogger(EnvironmentFactory.class.getName()).warning("No Enviroment service present, using NoopEnv!");
        return new NoopEnv();
    }

    private static class NoopEnv implements Environment {
        public String getDomain() {
            return "dummy";
        }

        public CapabilityState getState(Capability capability) {
            return ReflectionUtils.newInstance(
                    CapabilityState.class,
                    new Class[]{Capability.class, CapabilityStatus.class, long.class},
                    new Object[]{capability, CapabilityStatus.ENABLED, -1});
        }
    }
}
