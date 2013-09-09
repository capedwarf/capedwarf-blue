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

package org.jboss.capedwarf.aspects;

import org.jboss.capedwarf.aspects.proxy.AspectContext;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DisableSocketsAspect extends AbstractAspect<DisableSockets> {
    public DisableSocketsAspect() {
        super(DisableSockets.class);
    }

    public int priority() {
        return Integer.MIN_VALUE + 1; // after global limit
    }

    public Object invoke(AspectContext context) throws Throwable {
        Compatibility.enable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        try {
            return context.proceed();
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        }
    }
}
