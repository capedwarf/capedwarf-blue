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

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.IDatastoreServiceFactory;
import com.google.appengine.spi.FactoryProvider;
import com.google.appengine.spi.ServiceProvider;
import org.jboss.capedwarf.aspects.proxy.AspectFactory;
import org.jboss.capedwarf.aspects.proxy.AspectRegistry;
import org.jboss.capedwarf.common.spi.CapedwarfFactoryProvider;
import org.kohsuke.MetaInfServices;

/**
 * Datastore factory provider.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(FactoryProvider.class)
@ServiceProvider(value = IDatastoreServiceFactory.class, precedence = CapedwarfFactoryProvider.PRECEDENCE)
public class CapedwarfDatastoreServiceFactoryProvider extends CapedwarfFactoryProvider<IDatastoreServiceFactory> {
    static {
        AspectRegistry.addAspect(new TxTasksAspect());
        AspectRegistry.addAspect(new AutoTxAspect());
    }

    private IDatastoreServiceFactory factory = new IDatastoreServiceFactory() {
        public DatastoreService getDatastoreService(DatastoreServiceConfig config) {
            return AspectFactory.createProxy(ExposedDatastoreService.class, new CapedwarfDatastoreService(config));
        }

        public AsyncDatastoreService getAsyncDatastoreService(DatastoreServiceConfig config) {
            return AspectFactory.createProxy(ExposedAsyncDatastoreService.class, new CapedwarfAsyncDatastoreService(config));
        }
    };

    public CapedwarfDatastoreServiceFactoryProvider() {
        super(IDatastoreServiceFactory.class);
    }

    protected IDatastoreServiceFactory getFactoryInstance() {
        return factory;
    }
}
