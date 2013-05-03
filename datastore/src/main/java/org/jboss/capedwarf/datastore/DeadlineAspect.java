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

package org.jboss.capedwarf.datastore;

import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.aspects.AbstractAspect;
import org.jboss.capedwarf.aspects.proxy.AspectContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
final class DeadlineAspect extends AbstractAspect<Deadline> {
    DeadlineAspect() {
        super(Deadline.class);
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE; // this should be last
    }

    public Object invoke(AspectContext context) throws Throwable {
        AbstractDatastoreService service = (AbstractDatastoreService) context.getInfo().getApiImpl();
        DatastoreServiceConfig config = service.getDatastoreServiceConfig();
        Double deadline = config.getDeadline();
        if (deadline != null) {
            long now = System.nanoTime();
            Object result = context.proceed();
            long diff = System.nanoTime() - now;
            long nanos = new Double(1000.0 * 1000.0 * deadline).longValue();
            if (diff > nanos) {
                throw new ApiProxy.ApiDeadlineExceededException("datastore", context.getInfo().getMethod().getName());
            }
            return result;
        } else {
            return context.proceed();
        }
    }
}
