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

package org.jboss.capedwarf.datastore.stats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import com.google.appengine.api.datastore.Entity;

/**
 * Total stats update
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractUpdate implements Update {
    protected static enum Signum {
        PLUS(1), MINUS(-1);
        private final int x;

        private Signum(int x) {
            this.x = x;
        }
    }

    protected final Entity trigger;
    protected final Signum signum;

    protected AbstractUpdate(Entity trigger, Signum signum) {
        this.trigger = trigger;
        this.signum = signum;
    }

    public Object taskKey() {
        return statsKind();
    }

    public void initialize(Entity entity) {
        entity.setProperty("count", 0L);
        entity.setProperty("bytes", 0L);
    }

    @Override
    public String statsNamespace() {
        return "";
    }

    public Entity update(Entity entity) {
        Entity updated = new Entity(entity.getKind());
        doUpdate(entity, updated);
        return updated;
    }

    protected void doUpdate(Entity current, Entity newEntity) {
        newEntity.setProperty("timestamp", System.currentTimeMillis());

        long count = toLong(current, "count");
        newEntity.setProperty("count", count + signum.x);

        long bytes = toLong(current, "bytes");
        newEntity.setProperty("bytes", bytes + (signum.x * countBytes(trigger)));
    }

    public Callable<Entity> toCallable() {
        return new UpdateKeyTask(this);
    }

    // TODO -- better impl
    protected static long countBytes(Entity entity) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(entity);
            out.flush();
            return baos.size();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot count entity: " + entity);
        }
    }

    protected static Long toLong(Entity entity, String property) {
        Object value = entity.getProperty(property);
        return (value != null) ? Number.class.cast(value).longValue() : null;
    }
}