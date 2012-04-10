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

package org.jboss.capedwarf.channel.manager;

import org.infinispan.remoting.transport.Address;
import org.jboss.capedwarf.channel.util.ClusterUtils;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class Channel {

    private String clientId;
    private long expirationTime;
    private String token;

    /**
     * The address of the cluster node with which the browser connection is actually established.
     */
    private Address connectedNode;

    public Channel(String clientId, long expirationTime, String token) {
        this.clientId = clientId;
        this.expirationTime = expirationTime;
        this.token = token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Address getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(Address connectedNode) {
        this.connectedNode = connectedNode;
    }

    public void sendMessage(String message) {
        submitTask(new SendMessageTask(getToken(), message));
    }

    public void close() {
        submitTask(new CloseChannelTask(getToken()));
    }

    private void submitTask(Callable<Void> task) {
        ClusterUtils.submitToAllNodes(task);
//        ClusterUtils.submitToNode(getConnectedNode(), task);  TODO: submit only to node which holds the connection for the channel
    }
}
