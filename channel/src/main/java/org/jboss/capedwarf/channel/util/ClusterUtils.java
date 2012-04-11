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

package org.jboss.capedwarf.channel.util;

import org.infinispan.AdvancedCache;
import org.infinispan.commands.read.DistributedExecuteCommand;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ClusterUtils {

    public static void submitToAllNodes(Callable<Void> task) {
        if (isStandalone()) {
            executeLocally(task);
        } else {
            executeOnAllNodes(task);
        }
    }

    public static boolean isStandalone() {
        return true;    // TODO
    }

    private static void executeLocally(Callable<Void> task) {
        try {
            task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void executeOnAllNodes(Callable<Void> task) {
        DistributedExecuteCommand<Void> command = new DistributedExecuteCommand<Void>(Collections.emptyList(), task);
        AdvancedCache cache = InfinispanUtils.getCache(CacheName.DEFAULT).getAdvancedCache();
        RpcManager rpc = cache.getRpcManager();
        rpc.broadcastRpcCommand(command, false);
    }

    public static void submitToNode(Address nodeAddress, Callable<Void> task) {
        AdvancedCache cache = InfinispanUtils.getCache(CacheName.DEFAULT).getAdvancedCache();
        RpcManager rpc = cache.getRpcManager();
        rpc.invokeRemotely(
                Collections.singleton(nodeAddress),
                new DistributedExecuteCommand<Void>(Collections.emptyList(), task),
                false);
    }

    private static Address getAddress(RpcManager rpc, String nodeAddress) {
        return null;  // TODO
    }
}
