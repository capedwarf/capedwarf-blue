/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.cron;

import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.capedwarf.shared.components.ComponentRegistry;
import org.jboss.capedwarf.shared.components.Keys;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.util.Utils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.FRAG2;
import org.jgroups.stack.ProtocolStack;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CronService implements Receiver {
    private CapewarfCron cron;

    private Address address;
    private ForkChannel fork;
    private Address master;

    private CronService() {
    }

    public static CronService create(ApplicationConfiguration configuration) {
        CronService service = new CronService();
        CapewarfCron cc = new CapewarfCron(configuration);
        if (cc.prepare()) {
            service.cron = cc;

            JChannel channel = ComponentRegistry.getInstance().getComponent(Keys.CHANNEL);
            service.address = channel.getAddress();

            final String appId = configuration.getAppEngineWebXml().getApplication();
            final String module = configuration.getAppEngineWebXml().getModule();
            final String stackId = String.format("stack-%s_%s", appId, module);
            final String channelId = String.format("channel-%s_%s", appId, module);

            try {
                service.fork = new ForkChannel(channel, stackId, channelId, true, ProtocolStack.ABOVE, FRAG2.class);
                service.fork.setReceiver(service);
                service.fork.connect("ignored");
            } catch (Exception e) {
                throw Utils.toRuntimeException(e);
            }
        }

        return service;
    }

    public void destroy() {
        if (cron != null) {
            cron.destroy();
        }
        if (fork != null) {
            fork.close();
        }
    }

    public void viewAccepted(View view) {
        final Address[] addresses = view.getMembersRaw();
        // first is/will-be master
        final Address first = addresses[0];
        // has master changed?
        // shutdown all, only previous master will do real destroy
        boolean changed = master != null && master.compareTo(first) != 0;
        if (changed) {
            cron.destroy();
        }
        // are we master, if yes, start scheduler
        boolean isMaster = address.compareTo(first) == 0;
        if (isMaster && (changed || master == null)) {
            cron.start();
        }
        master = first;
    }

    public void suspect(Address suspect) {
    }

    public void block() {
    }

    public void unblock() {
    }

    public void receive(Message msg) {
    }

    public void getState(OutputStream output) throws Exception {
    }

    public void setState(InputStream input) throws Exception {
    }
}
