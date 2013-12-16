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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.quota.QuotaService;
import org.jboss.capedwarf.shared.reflection.ReflectionUtils;

/**
 * Abstract environment -- default impls.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractEnvironment implements Environment {
    protected static final String MASTER_SERVER_PARTITION = "";
    private static final QuotaService NOOP = new NoopQuotaService();

    private Map<Capability, CapabilityStatus> capabilityStatusMap = new ConcurrentHashMap<>();

    public CapabilityState getState(Capability capability) {
        CapabilityStatus status = capabilityStatusMap.get(capability);
        return ReflectionUtils.newInstance(
                CapabilityState.class,
                new Class[]{Capability.class, CapabilityStatus.class, long.class},
                new Object[]{capability, status == null ? CapabilityStatus.ENABLED : status, -1});
    }

    @Override
    public void setState(Capability capability, CapabilityStatus status) {
        capabilityStatusMap.put(capability, status);
    }

    public QuotaService getQuotaService() {
        return NOOP;
    }

    private static class NoopQuotaService implements QuotaService {
        public boolean supports(DataType dataType) {
            return false;
        }

        public long getApiTimeInMegaCycles() {
            return -1L;
        }

        public long getCpuTimeInMegaCycles() {
            return -1L;
        }

        public double convertMegacyclesToCpuSeconds(long l) {
            return -1;
        }

        public long convertCpuSecondsToMegacycles(double v) {
            return -1;
        }
    }
}
