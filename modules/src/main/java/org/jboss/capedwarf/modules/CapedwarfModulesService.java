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

package org.jboss.capedwarf.modules;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.labs.modules.ModulesService;
import org.jboss.capedwarf.common.config.CapedwarfEnvironment;
import org.jboss.capedwarf.common.shared.EnvAppIdFactory;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.MapKey;
import org.jboss.capedwarf.shared.components.Slot;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.ManualScaling;
import org.jboss.capedwarf.shared.config.Scaling;
import org.jboss.capedwarf.shared.modules.ModuleInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfModulesService implements ModulesService {
    private AppEngineWebXml appEngineWebXml;

    public CapedwarfModulesService() {
        appEngineWebXml = CapedwarfEnvironment.getThreadLocalInstance().getAppEngineWebXml();
    }

    private static Map<String, ModuleInfo> getModulesInfos() {
        return ComponentRegistry.getInstance().getComponent(new MapKey<String, ModuleInfo>(EnvAppIdFactory.INSTANCE, Slot.MODULES));
    }

    private static ModuleInfo getModuleInfo(String module) {
        ModuleInfo moduleInfo = getModulesInfos().get(module);
        if (moduleInfo == null) {
            throw new IllegalArgumentException("No such module: " + module);
        }
        return moduleInfo;
    }

    private static AppEngineWebXml getAppEngineWebXml(String module) {
        return getModuleInfo(module).getConfig();
    }

    private static boolean isCurrentVersion(String module, String version) {
        final AppEngineWebXml appEngineWebXml = getAppEngineWebXml(module);
        return appEngineWebXml.getVersion().equals(version);
    }

    public String getCurrentModule() {
        return appEngineWebXml.getModule();
    }

    public String getCurrentVersion() {
        return appEngineWebXml.getVersion();
    }

    public String getCurrentInstanceId() {
        return appEngineWebXml.getApplication();
    }

    public Set<String> getModules() {
        return getModulesInfos().keySet();
    }

    public Set<String> getVersions(String module) {
        return Collections.singleton(getAppEngineWebXml(module).getVersion());
    }

    public String getDefaultVersion(String module) {
        return getVersions(module).iterator().next();
    }

    public long getNumInstances(String module, String version) {
        final AppEngineWebXml appEngineWebXml = getAppEngineWebXml(module);
        if (appEngineWebXml.getVersion().equals(version)) {
            final Scaling scaling = appEngineWebXml.getScaling();
            final Scaling.Type type = (scaling  == null) ? Scaling.Type.AUTOMATIC : scaling.getType();
            switch (type) {
                case MANUAL:
                    return scaling.narrow(ManualScaling.class).getInstances();
                default:
                    return 1; // basic and auto have 1 instance
            }
        } else {
            return 0;
        }
    }

    public void setNumInstances(String module, String version, long instances) {
        // ignore atm
    }

    public void startModule(String module, String version) {
        if (isCurrentVersion(module, version)) {
            // DMR?
        }
    }

    public void stopModule(String module, String version) {
        if (isCurrentVersion(module, version)) {
            // DMR?
        }
    }

    public String getModuleHostname(String module, String version) {
        return getModuleHostname(module, version, 0);
    }

    public String getModuleHostname(String module, String version, int instance) {
        if (isCurrentVersion(module, version) == false) {
            return null;
        }

        ModuleInfo moduleInfo = getModuleInfo(module);
        return moduleInfo.getInstance(instance).getHostname();
    }

    public Future<Set<String>> getModulesAsync() {
        return ExecutorFactory.wrap(new Callable<Set<String>>() {
            public Set<String> call() throws Exception {
                return getModules();
            }
        });
    }

    public Future<Set<String>> getVersionsAsync(final String module) {
        return ExecutorFactory.wrap(new Callable<Set<String>>() {
            public Set<String> call() throws Exception {
                return getVersions(module);
            }
        });
    }

    public Future<String> getDefaultVersionAsync(final String module) {
        return ExecutorFactory.wrap(new Callable<String>() {
            public String call() throws Exception {
                return getDefaultVersion(module);
            }
        });
    }

    public Future<Long> getNumInstancesAsync(final String module, final String version) {
        return ExecutorFactory.wrap(new Callable<Long>() {
            public Long call() throws Exception {
                return getNumInstances(module, version);
            }
        });
    }

    public Future<Void> setNumInstancesAsync(final String module, final String version, final long instances) {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                setNumInstances(module, version, instances);
                return null;
            }
        });
    }

    public Future<Void> startModuleAsync(final String module, final String version) {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                startModule(module, version);
                return null;
            }
        });
    }

    public Future<Void> stopModuleAsync(final String module, final String version) {
        return ExecutorFactory.wrap(new Callable<Void>() {
            public Void call() throws Exception {
                startModule(module, version);
                return null;
            }
        });
    }

    public Future<String> getModuleHostnameAsync(final String module, final String version) {
        return ExecutorFactory.wrap(new Callable<String>() {
            public String call() throws Exception {
                return getModuleHostname(module, version);
            }
        });
    }

    public Future<String> getModuleHostnameAsync(final String module, final String version, final int instance) {
        return ExecutorFactory.wrap(new Callable<String>() {
            public String call() throws Exception {
                return getModuleHostname(module, version, instance);
            }
        });
    }
}
