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

import java.util.concurrent.Callable;

import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
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
        final String appId = Application.getAppId();
        InfinispanUtils.everywhere(appId, task);
    }

    public static void submitToNode(Address nodeAddress, Callable<Void> task) {
        final String appId = Application.getAppId();
        InfinispanUtils.single(appId, task, nodeAddress);
    }
}
