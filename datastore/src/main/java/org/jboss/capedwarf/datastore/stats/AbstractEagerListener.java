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

import com.google.appengine.api.datastore.Entity;
import org.jboss.capedwarf.datastore.notifications.AbstractPutRemoveCacheListener;

import static org.jboss.capedwarf.datastore.stats.AbstractUpdate.Signum.MINUS;
import static org.jboss.capedwarf.datastore.stats.AbstractUpdate.Signum.PLUS;

/**
 * Abstract Eager Listener.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AbstractEagerListener extends AbstractPutRemoveCacheListener {
    protected void onPostCreate(Entity trigger) {
        executeCallables(trigger, PLUS);
    }

    protected void onPrePut(Entity trigger) {
        executeCallables(trigger, MINUS);
    }

    protected void onPostPut(Entity trigger) {
        executeCallables(trigger, PLUS);
    }

    protected void onPreRemove(Entity trigger) {
        executeCallables(trigger, MINUS);
    }

    private void executeCallables(Entity trigger, AbstractUpdate.Signum signum) {
        executeCallable(new TotalStatsUpdate(trigger, signum));
        executeCallable(new NsTotalStatsUpdate(trigger, signum));
        executeCallable(new KindStatsUpdate(trigger, signum));
        executeCallable(new NsKindStatsUpdate(trigger, signum));
    }
}
