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

package org.jboss.capedwarf.tasks;

import java.io.Serializable;

import com.google.appengine.api.taskqueue.TaskOptions;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Indexed
@ProvidedId // TODO -- remove this once fixed in Ispan
public class TaskLeaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    static final String LEASE = "LEASE_";

    private TaskOptions options;

    public TaskLeaseEntity() {
    }

    public TaskLeaseEntity(TaskOptions options) {
        this.options = options;
    }

    public TaskOptions getOptions() {
        return options;
    }

    public void setOptions(TaskOptions options) {
        this.options = options;
    }
}
