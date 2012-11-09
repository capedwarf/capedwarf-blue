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

package org.jboss.capedwarf.tools;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BulkLoader {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
        }

        String command = args[0];
        if (command.equals("upload")) {
            Arguments arguments = new Arguments(args);
            doUpload(arguments);
        } else {
            System.out.println("Unknown command " + command);
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: BulkLoader upload --url=http://myapp.com/remote_api --filename=dump.sql3");
        System.exit(1);
    }

    private static void doUpload(Arguments args) {
        String url = args.get("--url");
        String filename = args.get("--filename");
        int packetSize = Integer.valueOf(args.get("--packetSize", "1000"));
        System.out.println("Uploading data from " + filename + " to " + url + " in packets of size " + packetSize);

        DumpFileReader dumpFileReader = new DumpFileReader(new File(filename));
        try {
            Iterator<byte[]> iterator = dumpFileReader.iterator();

            DefaultHttpClient client = new DefaultHttpClient(new SingleClientConnManager());
            try {
                File partFile = new File(filename + ".part");
                UploadPacket packet = new UploadPacket(partFile);
                while (iterator.hasNext()) {
                    byte[] pbBytes = iterator.next();
                    packet.add(pbBytes);

                    if (packet.size() == packetSize || !iterator.hasNext()) {
                        packet.close();
                        sendPacket(packet, client, url);
                        if (iterator.hasNext()) {
                            packet = new UploadPacket(partFile);
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            dumpFileReader.close();
        }
    }

    private static void sendPacket(UploadPacket packet, DefaultHttpClient client, String url) throws IOException {
        HttpPut put = new HttpPut(url);
        System.out.println("Uploading packet of " + packet.size() + " entities");
        put.setEntity(new FileEntity(packet.getFile(), "application/capedwarf-data"));
        HttpResponse response = client.execute(put);
        response.getEntity().writeTo(new ByteArrayOutputStream());
        System.out.println("Received response " + response);
    }


}
