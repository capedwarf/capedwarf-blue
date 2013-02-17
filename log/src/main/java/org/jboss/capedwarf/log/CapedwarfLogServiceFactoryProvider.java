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

package org.jboss.capedwarf.log;

import com.google.appengine.api.log.ILogServiceFactory;
import com.google.appengine.api.log.LogService;
import com.google.appengine.spi.FactoryProvider;
import com.google.appengine.spi.ServiceProvider;
import org.jboss.capedwarf.aspects.proxy.AspectFactory;
import org.jboss.capedwarf.common.spi.CapedwarfFactoryProvider;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(FactoryProvider.class)
@ServiceProvider(value = ILogServiceFactory.class, precedence = CapedwarfFactoryProvider.PRECEDENCE)
public class CapedwarfLogServiceFactoryProvider extends CapedwarfFactoryProvider<ILogServiceFactory> {
    private final ILogServiceFactory factory = new ILogServiceFactory() {
        public LogService getLogService() {
            return AspectFactory.createProxy(ExposedLogService.class, new CapedwarfLogService());
        }
    };

    public CapedwarfLogServiceFactoryProvider() {
        super(ILogServiceFactory.class);
    }

    protected ILogServiceFactory getFactoryInstance() {
        return factory;
    }
}
