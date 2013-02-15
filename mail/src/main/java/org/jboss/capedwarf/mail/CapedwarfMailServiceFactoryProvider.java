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

package org.jboss.capedwarf.mail;

import com.google.appengine.api.mail.IMailServiceFactory;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.spi.FactoryProvider;
import com.google.appengine.spi.ServiceProvider;
import org.jboss.capedwarf.common.spi.CapedwarfFactoryProvider;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(FactoryProvider.class)
@ServiceProvider(value = IMailServiceFactory.class, precedence = CapedwarfFactoryProvider.PRECEDENCE)
public class CapedwarfMailServiceFactoryProvider extends CapedwarfFactoryProvider<IMailServiceFactory> {
    private final IMailServiceFactory factory = new IMailServiceFactory() {
        public MailService getMailService() {
            return new CapedwarfMailService();
        }
    };

    public CapedwarfMailServiceFactoryProvider() {
        super(IMailServiceFactory.class);
    }

    protected IMailServiceFactory getFactoryInstance() {
        return factory;
    }
}
