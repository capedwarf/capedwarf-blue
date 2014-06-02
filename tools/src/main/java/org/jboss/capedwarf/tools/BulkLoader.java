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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BulkLoader {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
        }

        Arguments arguments = new Arguments(args);
        String command = args[0];
        switch (command) {
            case "upload":
                doUpload(arguments);
                break;
            case "dump":
                doDump(arguments);
                break;
            default:
                System.out.println("Unknown command " + command);
                printUsage();
                break;
        }
    }

    private static void printUsage() {
        System.out.println("Usage: BulkLoader upload --url=http://myapp.com/remote_api --filename=dump.sql3");
        System.exit(1);
    }

    private static void doUpload(Arguments args) {
        String url = getUrl(args);
        String filename = getFilename(args);
        int packetSize = getPacketSize(args);
        System.out.println("Uploading data from " + filename + " to " + url + " in packets of size " + packetSize);

        DumpFileFacade dumpFileFacade = new DumpFileFacade(new File(filename));
        try {
            Iterator<byte[]> iterator = dumpFileFacade.iterator();

            try (CloseableHttpClient client = HttpClients.createDefault()) {
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
            dumpFileFacade.close();
        }
    }

    private static void sendPacket(UploadPacket packet, HttpClient client, String url) throws IOException {
        HttpPut put = new HttpPut(url);
        System.out.println("Uploading packet of " + packet.size() + " entities");
        put.setEntity(new FileEntity(packet.getFile(), ContentType.create("application/capedwarf-data")));
        HttpResponse response = client.execute(put);
        response.getEntity().writeTo(new ByteArrayOutputStream());
        System.out.println("Received response " + response);
    }

    private static void doDump(Arguments args) {
        String url = getUrl(args);
        String filename = getFilename(args);
        int packetSize = getPacketSize(args);

        File file = new File(filename);
        if (file.exists()) {
            System.out.println("WARNING: File " + filename + " already exists!");
            System.exit(1);
        }

        System.out.println("Dumping data from " + url + " to " + filename + " in packets of size " + packetSize);

        DumpFileFacade dumpFileFacade = new DumpFileFacade(file);
        try {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet get = new HttpGet(url);
                get.getParams().setParameter("action", "dump");
                System.out.println("Downloading packet of " + packetSize + " entities");
                HttpResponse response = client.execute(get);
                DataInputStream in = new DataInputStream(response.getEntity().getContent());
                while (true) {
                    byte[] idBytes = readArray(in);
                    if (idBytes == null) {
                        break;
                    }
                    byte[] entityPbBytes = readArray(in);
                    byte[] sortKeyBytes = readArray(in);

                    dumpFileFacade.add(idBytes, entityPbBytes, sortKeyBytes);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            dumpFileFacade.close();
        }

    }

    private static byte[] readArray(DataInputStream in) {
        try {
            int arraySize;
            try {
                arraySize = in.readInt();
            } catch (EOFException e) {
                return null;
            }
            byte[] array = new byte[arraySize];
            in.readFully(array);
            return array;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Integer getPacketSize(Arguments args) {
        return Integer.valueOf(args.get("--packetSize", "1000"));
    }

    private static String getFilename(Arguments args) {
        return args.get("--filename");
    }

    private static String getUrl(Arguments args) {
        return args.get("--url");
    }


}
