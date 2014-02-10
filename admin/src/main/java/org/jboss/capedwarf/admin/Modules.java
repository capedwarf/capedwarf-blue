/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.jboss.capedwarf.shared.components.AppIdFactory;
import org.jboss.capedwarf.shared.config.AppEngineWebXml;
import org.jboss.capedwarf.shared.config.Scaling;
import org.jboss.capedwarf.shared.modules.ModuleInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Named("modules")
@RequestScoped
public class Modules {
    private static final String RUNNING = "Running";
    private List<Row> rows;

    public List<Row> getRows() {
        if (rows == null) {
            loadRows();
        }
        return rows;
    }

    @PostConstruct
    private void loadRows() {
        rows = new ArrayList<Row>();
        Map<String,ModuleInfo> modules = ModuleInfo.getModules(AppIdFactory.getAppId());
        for (Map.Entry<String, ModuleInfo> entry : modules.entrySet()) {
            ModuleInfo info = entry.getValue();
            AppEngineWebXml xml = info.getConfig();
            Scaling.Type type = (xml.getScaling() != null && xml.getScaling().getType() != null) ? xml.getScaling().getType() : Scaling.Type.AUTOMATIC;
            for (int i = 0; i < info.getInstancesSize(); i++) {
                this.rows.add(new Row(entry.getKey(), RUNNING, xml.getVersion(), info.getInstance(i).getHostname(), type.name(), 1));
            }
        }
    }

    public static class Row {
        private String name;
        private String state;
        private String version;
        private String hostname;
        private String scaling;
        private int instances;

        public Row(String name, String state, String version, String hostname, String scaling, int instances) {
            this.name = name;
            this.state = state;
            this.version = version;
            this.hostname = hostname;
            this.scaling = scaling;
            this.instances = instances;
        }

        public String getName() {
            return name;
        }

        public String getState() {
            return state;
        }

        public String getVersion() {
            return version;
        }

        public String getHostname() {
            return hostname;
        }

        public String getScaling() {
            return scaling;
        }

        public int getInstances() {
            return instances;
        }
    }

}
