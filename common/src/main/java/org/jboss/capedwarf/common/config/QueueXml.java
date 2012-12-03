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

package org.jboss.capedwarf.common.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class QueueXml implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Queue> queues = new ArrayList<Queue>();

    public void addQueue(String name, Mode mode) {
        queues.add(new Queue(name, mode));
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public static class Queue {
        private String name;
        private Mode mode;

        public Queue(String name, Mode mode) {
            this.name = name;
            this.mode = mode;
        }

        public String getName() {
            return name;
        }

        public Mode getMode() {
            return mode;
        }
    }

    public static enum Mode {
        PULL, PUSH
    }
}
