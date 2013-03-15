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

package org.jboss.capedwarf.datastore.metadata;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import org.infinispan.notifications.Listener;
import org.jboss.capedwarf.datastore.notifications.AbstractPutRemoveCacheListener;
import org.jboss.capedwarf.datastore.notifications.CacheListenerHandle;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Listener
public class MetadataListener extends AbstractPutRemoveCacheListener implements CacheListenerHandle {
    private static final Logger log = Logger.getLogger(MetadataListener.class.getName());

    public Object createListener(ClassLoader cl) {
        return new MetadataListener();
    }

    protected void executeCallable(MetadataTask task) {
        try {
            task.call();
        } catch (Throwable t) {
            log.warning("Cannot update metadata: " + t.getMessage());
        }
    }

    protected void onPrePut(Entity trigger) {
        // namespace and kind are immutable
    }

    protected void onPostPut(Entity trigger) {
        MetadataQueryTypeFactory.setFlag(true);
        try {
            executeCallable(new NamespaceMetadataTask(trigger.getNamespace(), true));
            executeCallable(new KindMetadataTask(trigger.getKind(), true, trigger.getNamespace()));
        } finally {
            MetadataQueryTypeFactory.setFlag(false);
        }
    }

    protected void onPreRemove(Entity trigger) {
        MetadataQueryTypeFactory.setFlag(true);
        try {
            executeCallable(new NamespaceMetadataTask(trigger.getNamespace(), false));
            executeCallable(new KindMetadataTask(trigger.getKind(), false, trigger.getNamespace()));
        } finally {
            MetadataQueryTypeFactory.setFlag(false);
        }
    }
}
