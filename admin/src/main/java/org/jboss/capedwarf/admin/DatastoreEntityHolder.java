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

import java.beans.PropertyEditor;

import javax.inject.Inject;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import org.jboss.capedwarf.shared.util.Utils;
import org.jboss.util.propertyeditor.PropertyEditors;

/**
 * @author Marko Luksa
 * @author Ales Justin
 */
public abstract class DatastoreEntityHolder extends DatastoreHolder {
    static {
        PropertyEditors.init();
    }

    static boolean hasPropertyEditor(Object value) {
        if (value == null) {
            return false;
        }
        try {
            PropertyEditor pe = PropertyEditors.findEditor(value.getClass());
            return (pe != null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Inject @HttpParam
    private String key;

    public String getKey() {
        return key;
    }

    public Key getReadableKey() {
        return KeyFactory.stringToKey(getKey());
    }

    protected Entity getEntity() throws EntityNotFoundException {
        return getDatastore().get(getReadableKey());
    }

    protected Entity getEntityUnchecked()  {
        try {
            return getEntity();
        } catch (EntityNotFoundException e) {
            throw Utils.toRuntimeException(e);
        }
    }
}
