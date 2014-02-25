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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.util.propertyeditor.PropertyEditors;

/**
 * @author Marko Luksa
 * @author Ales Justin
 */
@Named("datastoreEntity")
@RequestScoped
public class DatastoreEntityViewer extends DatastoreEntityHolder {
    @Inject @HttpParam
    private String action;

    @PostConstruct
    public void init() {
        if ("save".equals(action)) {
            Entity entity = getEntityUnchecked();
            HttpServletRequest request = CapedwarfVelocityContext.getInstance().getRequest();
            Enumeration<String> keys = request.getParameterNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.startsWith("__edit__")) {
                    String name = key.substring(8);
                    String value = request.getParameter(key);
                    String type = request.getParameter("__type__" + name);
                    Object result = null;
                    if ("NULL".equals(type) == false) {
                        Class<?> clazz = loadClass(type);
                        PropertyEditor pe = PropertyEditors.findEditor(clazz);
                        pe.setAsText(value);
                        result = pe.getValue();
                    }
                    entity.setProperty(name, result);
                }
            }
            getDatastore().put(entity);
        }
    }

    private Class<?> loadClass(final String type) {
        try {
            return Application.getAppClassLoader().loadClass(type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map.Entry<String, Object>> getProperties() {
        try {
            Entity entity = getEntity();
            return new ArrayList<Map.Entry<String, Object>>(entity.getProperties().entrySet());
        } catch (EntityNotFoundException e) {
            return Collections.emptyList();
        }
    }
}
