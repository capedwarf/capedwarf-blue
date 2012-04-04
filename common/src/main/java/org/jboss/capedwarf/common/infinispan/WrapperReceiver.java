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

package org.jboss.capedwarf.common.infinispan;

import java.io.InputStream;
import java.io.OutputStream;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

/**
 * Wrap receive call into our CL as TCCL.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class WrapperReceiver implements Receiver {
    private Receiver delegate;

    WrapperReceiver(Receiver delegate) {
        this.delegate = delegate;
    }

    public void viewAccepted(View new_view) {
        delegate.viewAccepted(new_view);
    }

    public void suspect(Address suspected_mbr) {
        delegate.suspect(suspected_mbr);
    }

    public void block() {
        delegate.block();
    }

    public void unblock() {
        delegate.unblock();
    }

    public void receive(Message msg) {
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            delegate.receive(msg);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    public void getState(OutputStream output) throws Exception {
        delegate.getState(output);
    }

    public void setState(InputStream input) throws Exception {
        delegate.setState(input);
    }
}
